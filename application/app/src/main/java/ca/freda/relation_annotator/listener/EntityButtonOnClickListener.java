package ca.freda.relation_annotator.listener;

import android.view.View;
import android.widget.Button;

import ca.freda.relation_annotator.fragment.AnnotationFragment;
import ca.freda.relation_annotator.fragment.RE.REAnnotationFragment;

public class EntityButtonOnClickListener implements View.OnClickListener {

    public AnnotationFragment annotationFragment;

    public EntityButtonOnClickListener(AnnotationFragment annotationFragment) {
        this.annotationFragment = annotationFragment;
    }

    @Override
    public void onClick(View v) {
        int tag = (int)v.getTag();
        Button button = (Button) v;
        annotationFragment.entityButtonClicked(tag,button);

    }
}
