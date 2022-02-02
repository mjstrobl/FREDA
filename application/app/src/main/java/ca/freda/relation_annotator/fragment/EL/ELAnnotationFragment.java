package ca.freda.relation_annotator.fragment.EL;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Message;
import android.text.Html;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import ca.freda.relation_annotator.MainActivity;
import ca.freda.relation_annotator.R;
import ca.freda.relation_annotator.data.Data;
import ca.freda.relation_annotator.data.Entity;
import ca.freda.relation_annotator.data.EntityButtonProperty;
import ca.freda.relation_annotator.data.Position;
import ca.freda.relation_annotator.fragment.AnnotationFragment;
import ca.freda.relation_annotator.fragment.NER.EditNameDialogFragment;
import ca.freda.relation_annotator.listener.EntityButtonOnClickListener;
import ca.freda.relation_annotator.listener.EntityScrollviewDragEventListener;
import ca.freda.relation_annotator.listener.EntityViewLongClickListener;
import ca.freda.relation_annotator.listener.GarbageViewDragEventListener;
import ca.freda.relation_annotator.listener.WordButtonDragEventListener;
import ca.freda.relation_annotator.listener.WordButtonOnTouchListener;
import ca.freda.relation_annotator.util.Utils;

public class ELAnnotationFragment extends AnnotationFragment implements View.OnClickListener {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.el_annotation_slide_page, container, false);
        super.fillRootView();
        return rootView;
    }


    @Override
    public void setUserVisibleHint(boolean visible) {
        super.setUserVisibleHint(visible);
    }


    protected Map<String,Integer> setPossibleResponses() {
        Map<String,Integer> possibleResponses = new HashMap<>();
        possibleResponses.put("done",0);
        return possibleResponses;
    }

    protected void setVariables() {
        task = "EL";
        overviewPagerItem = 3;
    }

    protected void setDatasetTextviewText(int annotator) {
        TextView datasetTextView = rootView.findViewById(R.id.dataset_textview);
        datasetTextView.setText("Annotator: " + annotator + ", Dataset: " + currentDatasetInfo);
    }


    protected void fillEntities(JSONObject sampleObject) throws JSONException {
        JSONArray entities = new JSONArray();
        for (int i = 0; i < data.getNumEntities(); i++) {
            Entity entity = data.getEntity(i);

            JSONArray positionsJSON = new JSONArray();
            // we can assume that all of these entities have at least one position.
            for (Position position : entity.getPositions()) {
                JSONArray positionJSON = new JSONArray();
                positionJSON.put(position.start);
                positionJSON.put(position.length);
                positionsJSON.put(positionJSON);
            }

            JSONObject entityJSON = new JSONObject();
            entityJSON.put("name", entity.getName());
            if (entity.getWikiName() != null) {
                entityJSON.put("wiki_name",entity.getWikiName());
            }
            entityJSON.put("positions",positionsJSON);
            entities.put(entityJSON);
        }
        sampleObject.put("entities",entities);
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
            TextView relationTextView = rootView.findViewById(R.id.el_textview);
            relationTextView.setText("Annotator: " + annotator + ", Dataset: " + currentDatasetName);

            Set<Integer> occupiedPositions = new HashSet<>();
            for(int i = 0; i < annotations.length(); i++) {
                JSONObject annotation = annotations.getJSONObject(i);
                String wikiName = null;
                if (annotation.has("wiki_name")) {
                    wikiName = annotation.getString("wiki_name");
                    System.out.println("found wiki name: " + wikiName);
                }

                JSONArray positionsJSON = annotation.getJSONArray("positions");
                List<Position> positions = new ArrayList<>(positionsJSON.length());
                for (int j = 0; j < positionsJSON.length(); j++) {
                    JSONArray currentPosition = positionsJSON.getJSONArray(j);
                    int start = currentPosition.getInt(0);
                    int length = currentPosition.getInt(1);

                    if (!occupiedPositions.contains(start) && !occupiedPositions.contains(start + length)) {
                        positions.add(new Position(start, length));
                        for (int k = start; k < start + length; k++) {
                            occupiedPositions.add(k);
                        }
                    }
                }
                if (positions.size() > 0) {
                    int colour = getNewEntityColour();
                    data.addEntity(colour, positions, wikiName);
                    usedColours.add(colour);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        reloadViews(true);
        switchButtonState(true);
    }

    protected void setEntityButtonListeners(Button button) {
        Entity entity = data.getEntity((int)button.getTag());
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    JSONObject message = new JSONObject();
                    message.put("mode", 6);
                    message.put("task", "EL");
                    message.put("index",button.getTag());
                    message.put("mention",entity.getName());
                    if (entity.getWikiName() != null) {
                        message.put("wikiName",entity.getWikiName());
                    }
                    activity.comHandler.sendMessage(message);
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
            }
        });
        button.setOnDragListener(new WordButtonDragEventListener(this,entity));
        button.setOnLongClickListener(new EntityViewLongClickListener());
    }

    public void showCandidates(JSONObject candidates, int index) {
        if (candidates.length() == 0) {
            activity.showToast("No candidates available for this mention!");
        } else {
            FragmentManager fm = getActivity().getSupportFragmentManager();
            CandidateSelectionDialog dialog = CandidateSelectionDialog.newInstance(candidates, index);
            dialog.setTargetFragment(this, 0);
            dialog.show(fm, "candidate_selection_fragment");
        }


    }

    public Data getData() {
        return data;
    }
}
