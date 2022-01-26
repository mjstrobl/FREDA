package ca.freda.relation_annotator.listener;

import android.content.ClipDescription;
import android.view.DragEvent;
import android.view.View;

import ca.freda.relation_annotator.data.Entity;
import ca.freda.relation_annotator.fragment.AnnotationFragment;
import ca.freda.relation_annotator.fragment.RE.REAnnotationFragment;

public class WordButtonDragEventListener implements View.OnDragListener {

    public AnnotationFragment annotationFragment;
    private Entity entity;

    public WordButtonDragEventListener(AnnotationFragment annotationFragment, Entity entity) {
        this.annotationFragment = annotationFragment;
        this.entity = entity;
    }

    public boolean onDrag(View v, DragEvent event) {

        //System.out.println(event);
        final int action = event.getAction();
        switch(action) {

            case DragEvent.ACTION_DRAG_STARTED: {

                String text = (String)event.getClipDescription().getLabel();
                System.out.println("ACTION_DRAG_STARTED: " + text);
                if (text.contains("wordview") && event.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                    v.invalidate();
                    return true;
                }

                return false;
            }

            case DragEvent.ACTION_DRAG_ENTERED:
                v.invalidate();
                return true;

            case DragEvent.ACTION_DRAG_LOCATION:
                return true;

            case DragEvent.ACTION_DRAG_EXITED:
                v.invalidate();
                return true;

            case DragEvent.ACTION_DROP: {
                String text = (String)event.getClipDescription().getLabel();
                System.out.println("ACTION_DROP: " + text);
                String[] tokens = text.split("_");
                System.out.println("Item text: " + text);
                int wordViewId = Integer.parseInt(tokens[tokens.length-1]);

                System.out.println("Dropped view: " + wordViewId);
                annotationFragment.addEntity(wordViewId,entity);


                annotationFragment.reloadViews(true);
                v.invalidate();
                return true;
            }
            case DragEvent.ACTION_DRAG_ENDED: {


                /*String text = (String)v.getTag();
                System.out.println("ACTION_DRAG_ENDED: " + text);
                String[] tokens = text.split("_");
                System.out.println("Item text: " + text);
                int tag = Integer.parseInt(tokens[tokens.length-1]);

                mainActivity.wordButtonDragEnded(text);
                System.out.println("text: " + text);*/
                v.invalidate();
                return true;
            }
            default:
                System.out.println("Unknown action type received by OnDragListener.");
                break;
        }

        System.out.println("drop result false");
        return false;
    }
}