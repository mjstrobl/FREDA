package ca.freda.relation_annotator.data;

import ca.freda.relation_annotator.util.Utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Entity {

    private List<Position> positions;
    private List<Position> textviewTextPositions;
    private String name;
    private EntityButtonProperty property;
    private int colour;
    private String wikiName;
    private String type;
    private Set<String> restrictedWords = new HashSet<>(Arrays.asList("who", "he", "she", "his", "him", "her",
            "herself", "himself", "whom", "whose","where","which","this","that","we","us","our","ours","I","me","my","mine","myself",
            "ouselves","you","your","yours","yourself","yourselves","hers","it","its","itself","they","them","their","theirs","themself",
            "themselves","this","one","one's","oneself","these","what","something","anything","nothing","that","someone","anyone","no one",
            "those","somebody","anybody","nobody","former","latter","when"));

    public Entity(int colour, EntityButtonProperty property, List<Position> positions) {
        this.colour = colour;
        this.positions = positions;
        this.property = property;
    }

    public Entity(int colour, List<Position> positions, String wikiName) {
        this.colour = colour;
        this.positions = positions;
        this.wikiName = wikiName;
        if (wikiName == null) {
            this.property = EntityButtonProperty.NONE;
        } else {
            this.property = EntityButtonProperty.WIKINAME;
        }
    }

    public void increaseProperty() {
        if (property == EntityButtonProperty.SUBJECT) {
            property = EntityButtonProperty.OBJECT;
        } else if (property == EntityButtonProperty.OBJECT) {
            property = EntityButtonProperty.NONE;
        } else {
            property = EntityButtonProperty.SUBJECT;
        }
    }

    public int getColour() {
        return colour;
    }

    public void addPosition(int start, int length, Data data) {
        positions.add(new Position(start, length));
        Collections.sort(positions, new Comparator<Position>(){

            @Override
            public int compare(Position p1, Position p2) {
                return Integer.compare(p1.start, p2.start);
            }

        });

        if (positions.size() > 1) {
            for (int i = 0; i < positions.size() - 1; i++) {
                Position firstPosition = positions.get(i);
                Position secondPosition = positions.get(i + 1);

                String firstWord = data.getSentence().substring(firstPosition.start, firstPosition.start + firstPosition.length);
                String secondWord = data.getSentence().substring(secondPosition.start, secondPosition.start + secondPosition.length);

                System.out.println("first word: " + firstWord);
                System.out.println("second word: " + secondWord);

                //if (restrictedWords.contains(firstWord) || restrictedWords.contains(secondWord)) {
                if (restrictedWords.contains(secondWord)) {
                    continue;
                }
                int difference = secondPosition.start - firstPosition.start - firstPosition.length;
                if (difference <= 1) {
                    Position newPosition = new Position(firstPosition.start, secondPosition.start + secondPosition.length - firstPosition.start);
                    positions.remove(i);
                    positions.remove(i);
                    positions.add(i,newPosition);
                    i--;
                }
            }
        }
        findName(data);
    }

    public void findName(Data data) {
        String bestName = "";

        for (Position position : positions) {
            String currentName = data.getSentence().substring(position.start, position.start + position.length);
            if (currentName.length() > bestName.length() && Utils.checkStringForUppercase(currentName)) {
                bestName = currentName;
            }
        }
        if (bestName.length() == 0) {
            for (Position position : positions) {
                String currentName = data.getSentence().substring(position.start, position.start + position.length);
                if (currentName.length() > bestName.length()) {
                    bestName = currentName;
                }
            }
        }

        name = bestName;
    }

    /*public String getWikiName() {
        return wikiName;
    }*/

    public void setPositions(List<Position> positions, Data data) {
        this.positions = positions;
        findName(data);
    }

    public List<Position> getPositions() {
        return positions;
    }

    public EntityButtonProperty getProperty() {
        return property;
    }

    public void setProperty(EntityButtonProperty property) {
        this.property = property;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        if (type == null) {
            this.property = EntityButtonProperty.NONE;
        } else {
            this.property = EntityButtonProperty.NERTYPE;
        }

        this.type = type;
    }

    public String getWikiName() {
        return wikiName;
    }

    public void setWikiName(String wikiName) {
        if (wikiName == null) {
            this.property = EntityButtonProperty.NONE;
        } else {
            this.property = EntityButtonProperty.WIKINAME;
        }
        this.wikiName = wikiName;
    }

    public List<Position> getTextviewTextPositions() {
        return textviewTextPositions;
    }

    public void setTextviewTextPositions(List<Position> textviewTextPositions) {
        this.textviewTextPositions = textviewTextPositions;
    }
}
