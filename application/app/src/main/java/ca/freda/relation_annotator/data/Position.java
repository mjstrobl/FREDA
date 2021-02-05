package ca.freda.relation_annotator.data;

public class Position {

    public int start;
    public int length;

    public Position(int start, int length) {
        this.start = start;
        this.length = length;
    }

    public boolean contains(int start, int length) {
        if (start >= this.start + this.length) {
            return false;
        } else if (start + length <= this.start) {
            return false;
        }

        return true;
    }
}
