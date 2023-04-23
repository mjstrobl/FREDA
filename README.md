# IMPORTANT

This is still in progress and I will update this repository soon after the conference (ACM SAC 2023). See branch thesis for a working version. Unfortunately, the app is not yet available in the Play Store, will come sometime in April 2023 though.

# FREDA

Fast Relation Extraction Data Annotation

See our paper on Arxiv (published in the Knowledge and Natural Language Processing track at ACM SAC
2023): https://arxiv.org/abs/2204.07150

FREDA can be used to manually annotate sentences quickly and accurately. A simple procedure for sentence acquisition from a partially annotated Wikipedia-based corpus is provided to be able to create datasets for new relations.

Current database (`database/*.jsonl`) contains at least 500 annotated sentences for 19 relations. In addition, four more relations are added without any annotations so far.

Each file contains data for a specific relation (see filename) and each line consists of:

- sentence
- entities: list of lists, one entry per entity; each entity consists of one or more positions (start,length) in the sentence.
- subjects: indexes of entities
- objects: indexes of entities
- response: 1 for relation holds between subjects and objects, 0 if not (subjects and objects irrelevant)

# Acknowledgements

We would like to thank all the data annotators for their hard work towards creating these datasets.
