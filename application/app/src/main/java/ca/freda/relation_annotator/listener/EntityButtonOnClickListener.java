package ca.freda.relation_annotator.listener;

import android.view.View;
import android.widget.Button;

import ca.freda.relation_annotator.fragment.AnnotationFragment;

public class EntityButtonOnClickListener implements View.OnClickListener {

    public AnnotationFragment mainActivity;

    public EntityButtonOnClickListener(AnnotationFragment mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void onClick(View v) {
        int tag = (int)v.getTag();
        Button button = (Button) v;
        mainActivity.entityButtonClicked(tag,button);

    }
}
