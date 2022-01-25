package ca.freda.relation_annotator.listener;

import android.content.ClipData;
import android.view.View;

public class EntityViewClickListener implements View.OnClickListener {
    @Override
    public void onClick(View v) {
        String label = "entity_" + v.getTag();

        ClipData item = ClipData.newPlainText(label, label);

        View.DragShadowBuilder myShadow = new WordButtonDragShadowBuilder(v);
        v.startDragAndDrop(item, myShadow, null, 0);
    }
}
