package ca.freda.relation_annotator.data;

import java.util.*;



public class Data {


    private String sentence;
    private List<Entity> entities;
    private List<Map<String,Object>> words;
    private Map<Integer,List<Entity>> entityAnnotations;

    public Data(String sentence) {
        this.entities = new ArrayList<>();
        this.sentence = sentence;
    }

    public Entity getEntity(int index) {
        return entities.get(index);
    }

    public void removeEntity(int index) {
        entities.remove(index);
    }

    public int getNumEntities() {
        return entities.size();
    }



    public void addEntity(int colour, List<Position> positions, String wikiName) {
        Entity entity = new Entity(colour,positions, wikiName);
        entity.findName(this);
        entities.add(entity);

        Collections.sort(entities, new Comparator<Entity>() {

            public int compare(Entity o1, Entity o2) {
                return Integer.compare(o1.getPositions().get(0).start,o2.getPositions().get(0).start);
            }
        });

    }

    public void addEntity(int colour, EntityButtonProperty property, List<Position> positions) {
        Entity entity = new Entity(colour,property,positions);
        entity.findName(this);
        entities.add(entity);

        Collections.sort(entities, new Comparator<Entity>() {

            public int compare(Entity o1, Entity o2) {
                return Integer.compare(o1.getPositions().get(0).start,o2.getPositions().get(0).start);
            }
        });

    }


    public void createAnnotations() {
        entityAnnotations = new HashMap<>();
        for (int i = 0; i < entities.size(); i++) {
            Entity entity = entities.get(i);
            List<Position> positions = entity.getPositions();
            for (Position position : positions) {
                int start = position.start;
                int length = position.length;
                for (int j = start; j < start+length; j++) {
                    if (!entityAnnotations.containsKey(j)) {
                        entityAnnotations.put(j,new ArrayList<Entity>());
                    }
                    entityAnnotations.get(j).add(entity);
                }
            }
        }
    }

    public void cleanUpEntities() {
        List<Entity> newEntities = new ArrayList<>();
        for (Entity entity : entities) {
            if (entity.getPositions().size() > 0) {
                newEntities.add(entity);
            }
        }
        entities = newEntities;
    }

    public void createWords() {
        words = new ArrayList<>();
        //String cleanSentence = sentence.replaceAll("[\\W]|_", " ");
        String cleanSentence = sentence;
        System.out.println("cleaned sentence: " + cleanSentence);
        String currentWord = "";
        int currentStart = 0;
        for (int i = 0; i < cleanSentence.length(); i++) {
            if (Character.isLetter(cleanSentence.charAt(i)) || Character.isDigit(cleanSentence.charAt(i))) {
                if (currentWord.length() == 0) {
                    currentStart = i;
                }
                currentWord += cleanSentence.charAt(i);
            } else if (cleanSentence.charAt(i) != ' ') {
                if (currentWord.length() > 0) {
                    addWord(currentWord,currentStart);
                    currentWord = "";
                }
                addWord("" + cleanSentence.charAt(i),i);
            } else if (currentWord.length() > 0) {
                addWord(currentWord,currentStart);
                currentWord = "";
            }
        }
        if (currentWord.length() > 0) {
            addWord(currentWord,currentStart);
            currentWord = "";
        }
    }

    private void addWord(String word, int start) {
        Map<String, Object> wordMap = new HashMap<>();
        wordMap.put("word", word);
        wordMap.put("start", start);
        wordMap.put("length", word.length());
        wordMap.put("entities",entityAnnotations.get(start));

        words.add(wordMap);
    }

    public String getSentence() {
        return sentence;
    }

    public List<Map<String,Object>> getWords() {
        return words;
    }

}
