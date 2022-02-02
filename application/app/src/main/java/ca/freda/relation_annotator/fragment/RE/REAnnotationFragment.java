package ca.freda.relation_annotator.fragment.RE;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.TextView;
import org.json.*;

import ca.freda.relation_annotator.data.EntityButtonProperty;
import ca.freda.relation_annotator.data.Position;
import ca.freda.relation_annotator.fragment.AnnotationFragment;
import ca.freda.relation_annotator.listener.*;
import ca.freda.relation_annotator.R;
import ca.freda.relation_annotator.data.Data;
import ca.freda.relation_annotator.data.Entity;
import ca.freda.relation_annotator.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class REAnnotationFragment extends AnnotationFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.re_annotation_slide_page, container, false);
        super.fillRootView();
        return rootView;
    }


    @Override
    public void setUserVisibleHint(boolean visible) {
        super.setUserVisibleHint(visible);
    }

    protected Map<String,Integer> setPossibleResponses() {
        Map<String,Integer> possibleResponses = new HashMap<>();
        possibleResponses.put("yes",1);
        possibleResponses.put("no",0);
        return possibleResponses;
    }

    protected void setVariables() {
        task = "RE";
        overviewPagerItem = 4;
    }

    protected void setDatasetTextviewText(int annotator) {
        TextView datasetTextView = rootView.findViewById(R.id.dataset_textview);
        datasetTextView.setText("Annotator: " + annotator + ", Relation: " + currentDatasetInfo);
    }

    protected void fillEntities(JSONObject sampleObject) throws JSONException {
        JSONArray entities = new JSONArray();
        JSONArray subjects = new JSONArray();
        JSONArray objects = new JSONArray();
        for (int i = 0; i < data.getNumEntities(); i++) {
            Entity entity = data.getEntity(i);
            super.fillEntity(entity, entities);

            if (entity.getProperty() == EntityButtonProperty.SUBJECT) {
                subjects.put(i);
            } else if (entity.getProperty() == EntityButtonProperty.OBJECT) {
                objects.put(i);
            }
        }
        sampleObject.put("subjects",subjects);
        sampleObject.put("objects",objects);

        JSONObject entityAnnotations = new JSONObject();
        entityAnnotations.put("entities",entities);
        entityAnnotations.put("task",task);

        sampleObject.put("entities",entityAnnotations);
    }

    public void createData(JSONObject message) {
        currentServerMessage = message;
        HorizontalScrollView scrollView = rootView.findViewById(R.id.entity_scrollview);
        scrollView.scrollTo(0, 0);

        try {
            // {"sentence": "In 2004, Kayla married David E. Tolbert.", "entity_subject": [[9, 5]], "entity_object": [[23, 16]], "id": 1, "mode": 1}
            usedColours = new HashSet<>();
            String sentence = currentServerMessage.getString("sentence");

            data = new Data(sentence);
            JSONArray annotations = currentServerMessage.getJSONObject("entities").getJSONArray("entities");

            int annotator = currentServerMessage.getInt("annotator");
            setDatasetTextviewText(annotator);

            for(int i = 0; i < annotations.length(); i++) {
                JSONObject annotation = annotations.getJSONObject(i);

                JSONArray positionsJSON = annotation.getJSONArray("positions");
                EntityButtonProperty property = EntityButtonProperty.NONE;

                List<Position> positions = new ArrayList<>(positionsJSON.length());
                for (int j = 0; j < positionsJSON.length(); j++) {
                    JSONArray currentPosition = positionsJSON.getJSONArray(j);
                    int start = currentPosition.getInt(0);
                    int length = currentPosition.getInt(1);
                    positions.add(new Position(start, length));
                }

                if (positions.size() > 0) {
                    int colour = getNewEntityColour();
                    data.addEntity(colour, property, positions);
                    usedColours.add(colour);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        reloadViews(true);
        super.switchButtonState(true);
    }

    protected void setEntityButtonListeners(Button button) {
        button.setOnClickListener(new EntityButtonOnClickListener(this));
        button.setOnDragListener(new WordButtonDragEventListener(this,data.getEntity((int)button.getTag())));
        button.setOnLongClickListener(new EntityViewLongClickListener());
    }
}
