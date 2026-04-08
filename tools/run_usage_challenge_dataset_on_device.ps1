param(
    [string]$DatasetPath = "C:\Users\r0xef\AndroidStudioProjects\LexicaAndroid2\app\src\test\resources\usage_challenge_dataset.csv",
    [ValidateSet("jaccard", "auto")]
    [string]$ValidatorMode = "jaccard",
    [string]$PackageName = "com.example.lexicaandroid2"
)

$ErrorActionPreference = "Stop"

function Get-AdbPath {
    $command = Get-Command adb -ErrorAction SilentlyContinue
    if ($command) { return $command.Source }

    $localProperties = "C:\Users\r0xef\AndroidStudioProjects\LexicaAndroid2\local.properties"
    if (Test-Path $localProperties) {
        $sdkLine = Get-Content $localProperties | Where-Object { $_ -like 'sdk.dir=*' } | Select-Object -First 1
        if ($sdkLine) {
            $sdkDir = $sdkLine.Substring(8).Replace('\\', '\')
            $adbPath = Join-Path $sdkDir 'platform-tools\adb.exe'
            if (Test-Path $adbPath) { return $adbPath }
        }
    }

    throw "adb introuvable"
}

function To-Base64([string]$text) {
    return [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($text))
}

function Invoke-UsageCheck($adb, $row, $validatorMode, $packageName) {
    & $adb shell am broadcast `
        -a "com.example.lexicaandroid2.DEBUG_USAGE_CHALLENGE" `
        --es word_b64 (To-Base64 $row.target_word) `
        --es definition_b64 (To-Base64 $row.expected_definition) `
        --es example_b64 (To-Base64 $row.example_hint) `
        --es sentence_b64 (To-Base64 $row.candidate_sentence) `
        --es validator_mode $validatorMode | Out-Null

    $json = & $adb shell run-as $packageName cat files/usage_challenge_last_result.json
    return $json | ConvertFrom-Json
}

$adb = Get-AdbPath
$devices = & $adb devices
if ($devices -notmatch "device`r?$") {
    throw "Aucun appareil/emulateur connecté"
}

$rows = Import-Csv -Path $DatasetPath -Delimiter ';'
$results = foreach ($row in $rows) {
    $result = Invoke-UsageCheck -adb $adb -row $row -validatorMode $ValidatorMode -packageName $PackageName
    [PSCustomObject]@{
        case_id = $row.case_id
        target_word = $row.target_word
        expected_verdict = $row.expected_verdict
        actual_verdict = $result.verdict
        is_valid = $result.isValid
        semantic_score = $result.semanticScore
        keyword_score = $result.keywordScore
        feedback = $result.feedbackMessage
        candidate_sentence = $row.candidate_sentence
    }
}

$results | Format-Table -AutoSize | Out-String | Write-Output

$falsePositives = ($results | Where-Object { $_.expected_verdict -eq 'REJECT' -and $_.actual_verdict -eq 'ACCEPT' }).Count
$exactMatches = ($results | Where-Object { $_.expected_verdict -eq $_.actual_verdict }).Count
$weighted = 0.0
foreach ($r in $results) {
    if ($r.expected_verdict -eq $r.actual_verdict) {
        $weighted += 1.0
    } elseif ($r.expected_verdict -eq 'BORDERLINE' -or $r.actual_verdict -eq 'BORDERLINE') {
        $weighted += 0.5
    }
}
$weighted = $weighted / [Math]::Max($results.Count, 1)

Write-Output ""
Write-Output "Résumé"
Write-Output "------"
Write-Output "Rows=$($results.Count)"
Write-Output "Exact=$exactMatches"
Write-Output "FalsePositives=$falsePositives"
Write-Output "WeightedScore=$weighted"

