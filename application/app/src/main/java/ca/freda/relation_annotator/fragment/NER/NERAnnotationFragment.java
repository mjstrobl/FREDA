package ca.freda.relation_annotator.fragment.NER;

import android.annotation.SuppressLint;
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
import android.view.MotionEvent;
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
import java.util.Iterator;
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
import ca.freda.relation_annotator.listener.EntityScrollviewDragEventListener;
import ca.freda.relation_annotator.listener.EntityViewLongClickListener;
import ca.freda.relation_annotator.listener.GarbageViewDragEventListener;
import ca.freda.relation_annotator.listener.EntityButtonOnTouchListener;
import ca.freda.relation_annotator.listener.TypeButtonDragEventListener;
import ca.freda.relation_annotator.listener.WordButtonDragEventListener;
import ca.freda.relation_annotator.listener.WordButtonOnTouchListener;

public class NERAnnotationFragment extends AnnotationFragment implements View.OnClickListener {

    private float start;
    private Map<String,ArrayList<String>> typeHierarchy;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.ner_annotation_slide_page, container, false);
        super.fillRootView();

        TextView textView = rootView.findViewById(R.id.textView);
        HorizontalScrollView scrollView = rootView.findViewById(R.id.entity_scrollview);
        textView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                System.out.println(event.getX());
                System.out.println(event.getAction());
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:

                        float delta = (event.getX() - start)/4;

                        System.out.println(delta);


                        scrollView.smoothScrollBy(Math.round(delta), 0);
                        break;
                    case MotionEvent.ACTION_DOWN:
                        start = event.getX();
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

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
        task = "NER";
        overviewPagerItem = 1;
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
            entityJSON.put("positions",positionsJSON);
            if (entity.getProperty() == EntityButtonProperty.NERTYPE) {
                entityJSON.put("type",entity.getType());
            }
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
            JSONArray annotations = currentServerMessage.getJSONArray("entities");


            if (currentDatasetName.contains("noentities")) {
                annotations = new JSONArray();
            }

            int annotator = currentServerMessage.getInt("annotator");
            setDatasetTextviewText(annotator);

            Set<Integer> occupiedPositions = new HashSet<>();
            for(int i = 0; i < annotations.length(); i++) {
                JSONObject annotation = annotations.getJSONObject(i);
                String wikiName = null;
                if (annotation.has("wiki_name")) {
                    wikiName = annotation.getString("wiki_name");
                    System.out.println("found wiki name: " + wikiName);
                }

                String name = null;
                if (annotation.has("name")) {
                    name = annotation.getString("name");
                    System.out.println("found name: " + name);
                } else {
                    name = wikiName;
                }

                JSONArray positionsJSON = annotation.getJSONArray("positions");
                EntityButtonProperty property = EntityButtonProperty.NONE;

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
                    data.addEntity(colour, property, positions);
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
        button.setOnTouchListener(new EntityButtonOnTouchListener(this));
        button.setOnDragListener(new WordButtonDragEventListener(this,data.getEntity((int)button.getTag())));
    }

    private void fillTypeButtonScrollView() {
        try {
            HorizontalScrollView scrollView = rootView.findViewById(R.id.type_scrollview);
            scrollView.removeAllViews();

            if (data == null) {
                return;
            }

            List<String> types = new ArrayList<>();

            if (activity.comHandler.getDataset().get("system").equals("flat")) {
                JSONArray typesJSON = activity.comHandler.getDataset().getJSONArray("types");
                for (int i = 0; i < typesJSON.length(); i++) {
                    String type = typesJSON.getString(i);
                    types.add(type);
                }
                typeHierarchy = null;
            } else {
                JSONObject typesJSON = activity.comHandler.getDataset().getJSONObject("types");
                Iterator<String> keys = typesJSON.keys();

                typeHierarchy = new HashMap<>();

                while(keys.hasNext()) {
                    String key = keys.next();
                    types.add(key);
                    JSONArray subTypesJSON = typesJSON.getJSONArray(key);
                    ArrayList<String> subTypes = new ArrayList<>(subTypesJSON.length());
                    for (int i = 0; i < subTypesJSON.length(); i++) {
                        subTypes.add(subTypesJSON.getString(i));
                    }
                    typeHierarchy.put(key,subTypes);
                }
            }

            System.out.println("fillTypeButtonScrollView");
            System.out.println("num types: " + types.size());

            LinearLayout layout = new LinearLayout(getActivity());
            linearHorizontalScrollviewLayout = layout;

            layout.setId(R.id.entity_scrollview_layout);
            layout.setOrientation(LinearLayout.HORIZONTAL);

            TypeButtonDragEventListener typeButtonDragEventListener = new TypeButtonDragEventListener(this);
            for (int i = 0; i < types.size(); i++) {
                String type = types.get(i);
                Button button = new Button(getActivity());
                button.setClickable(false);
                button.setActivated(false);
                button.setTypeface(null, Typeface.NORMAL);
                button.setTag(type);
                button.setText(type);

                button.setOnDragListener(typeButtonDragEventListener);

                linearHorizontalScrollviewLayout.addView(button);
            }

            scrollView.addView(layout);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }


    public void reloadViews(boolean reloadData) {
        super.reloadViews(reloadData);
        if (reloadData) {
            fillTypeButtonScrollView();
        }
    }

    public void setEntityType(String type, int index) {
        System.out.println("set type: " + type + ", for entity idx: " + index);
        data.getEntity(index).setType(type);

        if (typeHierarchy != null) {
            System.out.println("Found type hierarchy.");
            FragmentManager fm = getActivity().getSupportFragmentManager();
            EditNameDialogFragment editNameDialogFragment = EditNameDialogFragment.newInstance(typeHierarchy.get(type), index);
            editNameDialogFragment.setTargetFragment(this, 0);
            editNameDialogFragment.show(fm, "fragment_edit_name");
        } else {
            fillEntityButtonScrollView();
            fillTextView(false);
        }
    }

    public Data getData() {
        return data;
    }
}