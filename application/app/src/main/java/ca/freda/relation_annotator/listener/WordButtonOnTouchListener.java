package ca.freda.relation_annotator.listener;

import android.content.ClipData;
import android.view.MotionEvent;
import android.view.View;

import ca.freda.relation_annotator.fragment.AnnotationFragment;

public class WordButtonOnTouchListener implements View.OnTouchListener {


    public AnnotationFragment annotationFragment;

    public WordButtonOnTouchListener(AnnotationFragment annotationFragment) {
        this.annotationFragment = annotationFragment;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        System.out.println("on touch: " + event);
        final int action = event.getAction();

        switch(action) {

            case MotionEvent.ACTION_CANCEL: {
                return true;
            }
            case MotionEvent.ACTION_UP: {
                return true;
            }
            case MotionEvent.ACTION_DOWN: {
                ClipData item = ClipData.newPlainText("wordview_" + v.getTag(), "" + v.getTag());
                System.out.println("Item in action down: " + item);
                View.DragShadowBuilder myShadow = new WordButtonDragShadowBuilder(v);
                v.startDragAndDrop(item, myShadow, null, 0);
                return true;
            }
        }

        return true;
    }
}