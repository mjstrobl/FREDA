package ca.freda.relation_annotator.listener;

import android.graphics.Color;
import android.view.DragEvent;
import android.view.View;

import ca.freda.relation_annotator.fragment.AnnotationFragment;

public class GarbageViewDragEventListener implements View.OnDragListener {

    public AnnotationFragment mainActivity;

    public GarbageViewDragEventListener(AnnotationFragment mainActivity) {
        this.mainActivity = mainActivity;
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

            case DragEvent.ACTION_DRAG_ENTERED:
                System.out.println("ACTION_DRAG_ENTERED");
                v.setBackgroundColor(Color.GREEN);
                v.invalidate();

                return true;

            case DragEvent.ACTION_DRAG_LOCATION:
                //System.out.println("ACTION_DRAG_LOCATION");
                // Ignore the event
                return true;

            case DragEvent.ACTION_DRAG_EXITED:
                System.out.println("ACTION_DRAG_EXITED");
                v.setBackgroundColor(Color.LTGRAY);
                v.invalidate();

                return true;

            case DragEvent.ACTION_DROP:
                System.out.println("ACTION_DROP");
                // Gets the item containing the dragged data
                String text = (String)event.getClipDescription().getLabel();
                String[] tokens = text.split("_");
                System.out.println("Item text: " + text);
                int buttonId = Integer.parseInt(tokens[tokens.length-1]);
                if (text.contains("entity")) {
                    System.out.println("entity button id: " + buttonId);
                    mainActivity.removeEntity(buttonId);
                } else if (text.contains("wordview")) {
                    System.out.println("wordview id: " + buttonId);
                    mainActivity.removeWordFromPositions(buttonId);
                }


                return true;

            case DragEvent.ACTION_DRAG_ENDED:
                v.setBackgroundColor(Color.LTGRAY);
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