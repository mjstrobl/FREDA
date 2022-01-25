package ca.freda.relation_annotator.fragment;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import ca.freda.relation_annotator.data.Entity;


public abstract class AnnotationFragment  extends Fragment {

    public abstract void reloadViews();

    public abstract void addEntity(int wordViewId, Entity entityAttached);

    public abstract void removeEntity(int index);

    public abstract void removeWordFromPositions(int index);

    public abstract void entityButtonClicked(int index, Button button);

    public abstract void setEntityType(String type, int index);

}
