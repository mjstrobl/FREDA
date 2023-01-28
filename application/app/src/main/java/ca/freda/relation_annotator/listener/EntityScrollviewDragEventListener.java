package ca.freda.relation_annotator.listener;

import android.content.ClipDescription;
import android.view.DragEvent;
import android.view.View;

import ca.freda.relation_annotator.fragment.AnnotationFragment;

public class EntityScrollviewDragEventListener implements View.OnDragListener {

    private AnnotationFragment annotationFragment;

    public EntityScrollviewDragEventListener(AnnotationFragment annotationFragment) {
        this.annotationFragment = annotationFragment;
    }

    public boolean onDrag(View v, DragEvent event) {

        //System.out.println(event);
        final int action = event.getAction();
        switch(action) {

            case DragEvent.ACTION_DRAG_STARTED: {
                System.out.println("Scrollview ACTION_DRAG_STARTED");
                if (event.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                    String text = (String)event.getClipDescription().getLabel();
                    v.invalidate();
                    return true;
                }

                return false;
            }

            case DragEvent.ACTION_DRAG_ENTERED:
                v.invalidate();
                return true;

            case DragEvent.ACTION_DRAG_LOCATION:
                //System.out.println("Scrollview ACTION_DRAG_LOCATION");
                //System.out.println(event);

                //mainActivity.scrollEntityScrollView(event.getX(),scrollView);

                return true;

            case DragEvent.ACTION_DRAG_EXITED:
                v.invalidate();
                return true;

            case DragEvent.ACTION_DROP: {

                String text = (String)event.getClipDescription().getLabel();
                System.out.println("Scrollview ACTION_DROP: " + text);
                String[] tokens = text.split("_");
                System.out.println("Item text: " + text);

                int index = Integer.parseInt(tokens[tokens.length-1]);

                if (text.contains("wordview")) {
                    annotationFragment.addEntity(index, null);
                }

                annotationFragment.reloadViews(true);

                v.invalidate();
                return true;
            }
            case DragEvent.ACTION_DRAG_ENDED: {
                System.out.println("Scrollview ACTION_DRAG_ENDED");
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
