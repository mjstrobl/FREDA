package ca.freda.relation_annotator.listener;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.DragEvent;
import android.view.View;

import androidx.core.content.ContextCompat;

import ca.freda.relation_annotator.R;
import ca.freda.relation_annotator.fragment.AnnotationFragment;
import ca.freda.relation_annotator.fragment.NER.NERAnnotationFragment;

public class TypeButtonDragEventListener implements View.OnDragListener {

    public NERAnnotationFragment annotationFragment;

    public TypeButtonDragEventListener(NERAnnotationFragment annotationFragment) {
        this.annotationFragment = annotationFragment;
    }

    // This is the method that the system calls when it dispatches a drag event to the
    // listener.
    public boolean onDrag(View v, DragEvent event) {

        //System.out.println(event);

        // Defines a variable to store the action type for the incoming event
        final int action = event.getAction();

        // Handles each of the expected events
        switch(action) {

            case DragEvent.ACTION_DRAG_STARTED: {
                System.out.println("ACTION_DRAG_STARTED in GarbageViewDragEventListener");
                String text = (String)event.getClipDescription().getLabel();
                System.out.println(text);
                //if (text.contains("entity")) {
                v.invalidate();
                return true;
                //}
                //return false;
            }

            case DragEvent.ACTION_DRAG_ENTERED: {
                System.out.println("ACTION_DRAG_ENTERED");
                String text = (String) event.getClipDescription().getLabel();
                if (text.contains("entity")) {
                    //v.setBackgroundColor(Color.GREEN);
                    v.getBackground().setColorFilter(Color.GREEN,PorterDuff.Mode.MULTIPLY);
                }
                v.invalidate();

                return true;
            }
            case DragEvent.ACTION_DRAG_LOCATION:
                //System.out.println("ACTION_DRAG_LOCATION");
                // Ignore the event
                return true;

            case DragEvent.ACTION_DRAG_EXITED:
                System.out.println("ACTION_DRAG_EXITED");
                v.getBackground().setColorFilter(Color.LTGRAY,PorterDuff.Mode.MULTIPLY);
                v.invalidate();

                return true;

            case DragEvent.ACTION_DROP: {
                System.out.println("ACTION_DROP");
                // Gets the item containing the dragged data
                String text = (String) event.getClipDescription().getLabel();
                String[] tokens = text.split("_");
                System.out.println("Item text: " + text);
                int buttonId = Integer.parseInt(tokens[tokens.length - 1]);
                if (text.contains("entity")) {
                    System.out.println("entity button id: " + buttonId);
                    annotationFragment.setEntityType((String) v.getTag(), buttonId);
                }

                return true;
            }
            case DragEvent.ACTION_DRAG_ENDED:
                v.getBackground().setColorFilter(Color.LTGRAY,PorterDuff.Mode.MULTIPLY);
                return true;

            // An unknown action type was received.
            default:
                System.out.println("Unknown action type received by OnDragListener.");
                break;
        }

        System.out.println("drop result false");
        return false;
    }
}
