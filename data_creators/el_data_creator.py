import json
import re

RE_LINKS = re.compile(r'\[{2}(.*?)\]{2}', re.DOTALL | re.UNICODE)


def clean_line(line):
    while True:
        match = re.search(RE_LINKS, line)
        if match:
            start = match.start()
            end = match.end()
            entity = match.group(1)
            parts = entity.split('|')
            alias = parts[-2]
            line = line[:start] + alias + line[end:]
        else:
            break
    return line






#aliases = json.load(open("/media/michi/Data/wexea/final/en/dictionaries/aliases_pruned.json"))
#aliases_reverse = json.load(open("/media/michi/Data/wexea/final/en/dictionaries/aliases_reverse.json"))
title2filename = json.load(open("/media/michi/Data/wexea/final/en/dictionaries/title2filename.json"))

abstracts = {}
c = 0
for title in title2filename:
    filename = title2filename[title]
    c += 1

    if c % 1000 == 0:
        print("Processed: " + str(c), end='\r')

    with open(filename) as f:
        content = ''
        for line in f:
            line = line.strip()
            if len(content) > 0:
                break
            else:
                content += clean_line(line) + ' '


        abstracts[title] = content.strip()

with open('abstracts.json','w') as f:
    json.dump(abstracts,f)
