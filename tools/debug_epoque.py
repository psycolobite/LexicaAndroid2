import sys
sys.path.insert(0, '.')
from classifier.data_loaders import load_lexique, load_pageviews
from classifier.classifier_epoque import explain_epoque

lexique = load_lexique()
pageviews = load_pageviews()

words = ['jadis', 'obsolète', 'nostalgie', 'ephemere']
for w in words:
    print(explain_epoque(w, 'LITTERAIRE_STANDARD', lexique, pageviews))
    print()
