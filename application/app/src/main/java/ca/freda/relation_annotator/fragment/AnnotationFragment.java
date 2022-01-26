package ca.freda.relation_annotator.fragment;
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

import androidx.fragment.app.Fragment;

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
import ca.freda.relation_annotator.listener.EntityButtonOnClickListener;
import ca.freda.relation_annotator.listener.EntityScrollviewDragEventListener;
import ca.freda.relation_annotator.listener.EntityViewLongClickListener;
import ca.freda.relation_annotator.listener.GarbageViewDragEventListener;
import ca.freda.relation_annotator.listener.WordButtonDragEventListener;
import ca.freda.relation_annotator.listener.WordButtonOnTouchListener;
import ca.freda.relation_annotator.util.Utils;


public class AnnotationFragment extends Fragment implements View.OnClickListener {

    protected JSONObject currentServerMessage;

    protected String currentDatasetInfo;
    protected String currentDatasetName;
    protected MainActivity activity;

    protected String task;
    protected int overviewPagerItem;

    List<Map<String, Object>> allPositions;
    protected List<Button> responseButtons;

    protected ViewGroup rootView;
    protected Data data;
    protected LinearLayout linearHorizontalScrollviewLayout;
    //private String[] htmlColours = {"#009900","#0099ff","#ff5050","#9933ff","#ff8b33","#7e33ff","#260505","#220b80","#4bde7b","#65c900","#800046","#a65c46","#c0d40d"};
    protected String[] htmlColours = {"#e6194B", "#3cb44b", "#97850a", "#4363d8", "#f58231", "#911eb4", "#2c98b0", "#f032e6", "#5b9b22", "#cc6d91", "#30998e", "#9c63e1",
            "#9A6324", "#7f7b54", "#800000", "#538963", "#808000", "#694d32", "#000075", "#5a5a5a", "#000000"};
    protected Set<Integer> usedColours = new HashSet<>();

    @Override
    public void setUserVisibleHint(boolean visible) {
        super.setUserVisibleHint(visible);
        setVariables();
        if (visible) {
            try {
                TextView uidTextView = rootView.findViewById(R.id.uid_textview);

                currentDatasetName = activity.comHandler.getDataset().getString("name");
                if (activity.comHandler.getDataset().has("info_short")) {
                    currentDatasetInfo = activity.comHandler.getDataset().getString("info_short");
                } else {
                    currentDatasetInfo = currentDatasetName;
                }

                uidTextView.setText(activity.getUID());

                HorizontalScrollView scrollView = rootView.findViewById(R.id.response_scrollview);
                scrollView.removeAllViews();

                LinearLayout layout = new LinearLayout(getActivity());

                layout.setId(R.id.entity_scrollview_layout);
                layout.setOrientation(LinearLayout.HORIZONTAL);

                // For different tasks it is possible to use different responses, for now yes/no (apart from ignore/remove) is enough.
                Map<String,Integer> possibleResponses = setPossibleResponses();

                responseButtons = new ArrayList<>();

                for ( String key : possibleResponses.keySet()) {
                    int value = possibleResponses.get(key);

                    Button button = new Button(getActivity());
                    button.setTypeface(null, Typeface.NORMAL);
                    button.setTag(value);
                    SpannableString spanString = new SpannableString(key.toUpperCase());
                    spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, spanString.length(), 0);
                    button.setText(spanString);
                    button.setAllCaps(true);
                    button.setSingleLine(true);
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setTextViewText("Waiting for data, could take around 30 seconds.");
                            sendSampleResponse((int)v.getTag());
                        }
                    });
                    layout.addView(button);
                    responseButtons.add(button);
                }

                scrollView.addView(layout);

                switchButtonState(false);


                JSONObject message = new JSONObject();
                message.put("task", task);
                message.put("mode", 1);
                activity.comHandler.sendMessage(message);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException ex) {
                ex.printStackTrace();
            }

            System.out.println("Annotation Fragment visible.");
        }
    }

    protected void setVariables() {
    }

    protected Map<String,Integer> setPossibleResponses() {
        return null;
    }

    protected void fillRootView() {
        activity = (MainActivity) getActivity();

        HorizontalScrollView scrollView = rootView.findViewById(R.id.entity_scrollview);
        EntityScrollviewDragEventListener scrollviewDragEventListener = new EntityScrollviewDragEventListener(this);
        scrollView.setOnDragListener(scrollviewDragEventListener);

        TextView textView = rootView.findViewById(R.id.textView);
        textView.setOnDragListener(scrollviewDragEventListener);

        GarbageViewDragEventListener garbageViewDragEventListener = new GarbageViewDragEventListener(this);
        ImageButton trashButton = rootView.findViewById(R.id.trash);
        trashButton.setOnDragListener(garbageViewDragEventListener);

        rootView.findViewById(R.id.previous_button).setOnClickListener(this);
        rootView.findViewById(R.id.annotation_remove_button).setOnClickListener(this);
        rootView.findViewById(R.id.annotation_ignore_button).setOnClickListener(this);
        rootView.findViewById(R.id.reload_button).setOnClickListener(this);
        rootView.findViewById(R.id.main_button_annotation).setOnClickListener(this);

        System.out.println("on create annotation fragment.");
    }

    @Override
    public void onClick(View view) {
        System.out.println("onclick");
        switch (view.getId()) {
            case R.id.previous_button:
                switchButtonState(false);
                try {
                    JSONObject message = new JSONObject();
                    message.put("task", task);
                    message.put("mode", 4);
                    activity.comHandler.sendMessage(message);
                    message.put("mode", 1);
                    activity.comHandler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                break;
            case R.id.reload_button:
                if (currentServerMessage == null) {
                    try {
                        JSONObject message = new JSONObject();
                        message.put("task", task);
                        message.put("mode", 1);
                        activity.comHandler.sendMessage(message);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    createData(currentServerMessage);
                }
                break;
            case R.id.main_button_annotation:
                activity.setPagerItem(overviewPagerItem);
                break;
            case R.id.annotation_ignore_button:
                sendSampleResponse(-1);
                break;
            case R.id.annotation_remove_button:
                sendSampleResponse(-2);
                break;
            default:
                break;
        }
    }

    protected void switchButtonState(boolean enabled) {
        if (responseButtons != null) {
            rootView.findViewById(R.id.annotation_remove_button).setEnabled(enabled);
            rootView.findViewById(R.id.annotation_ignore_button).setEnabled(enabled);
            rootView.findViewById(R.id.main_button_annotation).setEnabled(enabled);
            rootView.findViewById(R.id.reload_button).setEnabled(enabled);
            rootView.findViewById(R.id.previous_button).setEnabled(enabled);
            for (Button button : responseButtons) {
                button.setEnabled(enabled);
            }
        }
    }

    private void sendSampleResponse(int response) {
        switchButtonState(false);
        Message msg = activity.comHandler.obtainMessage();

        try {
            if (msg != null) {
                JSONObject sampleObject = new JSONObject();
                sampleObject.put("mode", 2);
                sampleObject.put("response", response);
                sampleObject.put("dataset", currentDatasetName);
                sampleObject.put("uid", activity.getUID());
                sampleObject.put("article", currentServerMessage.getString("article"));
                sampleObject.put("line", currentServerMessage.getInt("line"));
                fillEntities(sampleObject);
                msg.obj = sampleObject.toString();
                System.out.println(msg.obj);
                activity.comHandler.sendMessage(msg);
            }

            JSONObject message = new JSONObject();
            message.put("task", task);
            message.put("mode", 1);
            activity.comHandler.sendMessage(message);
        } catch (JSONException ex) {
            ex.printStackTrace();
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    protected void fillEntities(JSONObject sampleObject) throws JSONException {

    }

    protected void fillEntity(Entity entity, JSONArray entities) throws JSONException {
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
        entities.put(entityJSON);
    }

    public void createData(JSONObject message) {

    }

    protected void setDatasetTextviewText(int annotator) {
        TextView datasetTextView = rootView.findViewById(R.id.dataset_textview);
        datasetTextView.setText("Annotator: " + annotator + ", Dataset: " + currentDatasetInfo);
    }

    public void fillTextView(boolean restart) {
        System.out.println("fillTextView");

        if (data == null) {
            setTextViewText("Waiting for data, could take around 30 seconds.");
            return;
        }

        if (restart || allPositions == null) {
            //sort entity positions
            allPositions = new ArrayList<>();
            for (int i = 0; i < data.getNumEntities(); i++) {
                Entity entity = data.getEntity(i);
                entity.setTextviewTextPositions(new ArrayList<>());
                for (Position position : entity.getPositions()) {
                    int start = position.start;
                    int length = position.length;
                    Map<String, Object> entityPosition = new HashMap<>();
                    entityPosition.put("start", start);
                    entityPosition.put("length", length);
                    entityPosition.put("entityIndex", i);
                    allPositions.add(entityPosition);
                }
            }

            Collections.sort(allPositions, new Comparator<Map<String, Object>>() {

                @Override
                public int compare(Map<String, Object> p1, Map<String, Object> p2) {
                    int start1 = (int) p1.get("start");
                    int length1 = (int) p1.get("length");
                    int start2 = (int) p2.get("start");
                    int length2 = (int) p2.get("length");

                    if (start1 == start2) {
                        return Integer.compare(length2, length1);
                    } else {
                        return Integer.compare(start1, start2);
                    }
                }

            });
        }

        String sentence = data.getSentence();
        for (int i = allPositions.size() - 1; i >= 0; i--) {
            int start = (int)allPositions.get(i).get("start");
            int length = (int)allPositions.get(i).get("length");
            Entity entity = data.getEntity((int)allPositions.get(i).get("entityIndex"));
            int colour = entity.getColour();
            EntityButtonProperty property = entity.getProperty();

            String before = sentence.substring(0, start);
            String after = sentence.substring(start + length);
            String mention = sentence.substring(start, start + length);

            System.out.println("Outer mention: " + mention);

            mention = "<span style=\"color: " + htmlColours[colour] + ";\">" + mention + "</span>";

            if (property != EntityButtonProperty.NONE) {
                sentence = before + "<b><u>" + mention + "</u></b>" + after;
            } else {
                sentence = before + "<b>" + mention + "</b>" + after;
            }
        }

        System.out.println(sentence);
        TextView textView = rootView.findViewById(R.id.textView);
        textView.setText(Html.fromHtml(sentence,0));
    }

    private void fillWordButtonView() {
        ScrollView scrollView = (ScrollView) rootView.findViewById(R.id.word_scrollview);
        scrollView.removeAllViews();

        if (data == null) {
            return;
        }

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int numColumns = 9;

        List<Map<String,Object>> words = data.getWords();

        TableLayout tableLayout = new TableLayout(getActivity());
        tableLayout.setOrientation(LinearLayout.VERTICAL);
        System.out.println("table layout: " + tableLayout);
        tableLayout.setShrinkAllColumns(true);
        tableLayout.setStretchAllColumns(true);

        int index = 0;
        int rowIndex = 1;
        TableRow tableRow = new TableRow(getActivity());
        while (index < words.size()) {
            if (rowIndex <= numColumns) {
                Button button = new Button(getActivity());

                String buttonText = (String)words.get(index).get("word");

                button.setText(buttonText);
                button.setTag(index);
                button.setEllipsize(TextUtils.TruncateAt.END);
                button.setSingleLine(true);

                button.setOnTouchListener(new WordButtonOnTouchListener(this));
                Entity entity = (Entity)words.get(index).get("entity");
                if (entity != null) {
                    int colour = entity.getColour();
                    int btnColour = new BigInteger("AA" + htmlColours[colour].substring(1), 16).intValue();
                    button.setTextColor(btnColour);
                }
                tableRow.addView(button);
                index++;
                rowIndex++;
            } else {
                rowIndex = 1;

                View emptyView = new View(getActivity());
                TableRow.LayoutParams separatorLayoutParams = new TableRow.LayoutParams(80, 1);
                separatorLayoutParams.setMargins(0, 0, 0, 0);
                tableRow.addView(emptyView,separatorLayoutParams);

                tableLayout.addView(tableRow);
                tableRow = new TableRow(getActivity());
            }
        }

        if (rowIndex > 0) {
            View emptyView = new View(getActivity());
            TableRow.LayoutParams separatorLayoutParams = new TableRow.LayoutParams(80, 1);
            separatorLayoutParams.setMargins(0, 0, 0, 0);
            tableRow.addView(emptyView,separatorLayoutParams);
            tableLayout.addView(tableRow);
        }

        scrollView.addView(tableLayout);
    }

    public void entityButtonClicked(int index, Button button, EntityButtonProperty property) {
        Entity entity = data.getEntity(index);
        if (property == null) {
            entity.increaseProperty();
        }

        setButtonLayout(button,entity);
        fillTextView(false);
        //fillWordButtonView();
    }

    public void fillEntityButtonScrollView() {
        HorizontalScrollView scrollView = rootView.findViewById(R.id.entity_scrollview);
        scrollView.removeAllViews();

        if (data == null) {
            return;
        }


        System.out.println("fillEntityButtonScrollView");
        System.out.println("num entities: " + data.getNumEntities());

        LinearLayout layout = new LinearLayout(getActivity());
        linearHorizontalScrollviewLayout = layout;

        layout.setId(R.id.entity_scrollview_layout);
        layout.setOrientation(LinearLayout.HORIZONTAL);

        for (int i = 0; i < data.getNumEntities(); i++) {
            Entity entity = data.getEntity(i);
            System.out.println(entity.toString());
            Button button = new Button(getActivity());
            button.setActivated(false);
            button.setTypeface(null, Typeface.NORMAL);
            button.setTag(i);
            setEntityButtonListeners(button);
            linearHorizontalScrollviewLayout.addView(button);

            setButtonLayout(button,entity);
        }

        scrollView.addView(layout);
    }

    protected void setEntityButtonListeners(Button button) {

    }

    private void setButtonLayout(Button button, Entity entity) {
        int colour = entity.getColour();
        System.out.println("colour: " + colour);
        int btnColour = new BigInteger("AA" + htmlColours[colour].substring(1), 16).intValue();
        button.setTextColor(btnColour);
        if (entity.getProperty() == EntityButtonProperty.SUBJECT) {
            button.setPaintFlags(button.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            button.setTypeface(null, Typeface.BOLD);
            button.setText(String.format("%s (subj)", entity.getName()));
        } else if (entity.getProperty() == EntityButtonProperty.OBJECT) {
            button.setPaintFlags(button.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            button.setTypeface(null, Typeface.BOLD);
            button.setText(String.format("%s (obj)", entity.getName()));
        } else if (entity.getProperty() == EntityButtonProperty.NERTYPE) {
            button.setPaintFlags(button.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            button.setTypeface(null, Typeface.BOLD);
            button.setText(String.format("%s (%s)", entity.getName(), entity.getType()));
        } else if (entity.getProperty() == EntityButtonProperty.NONE) {
            button.setPaintFlags(button.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
            button.setTypeface(null, Typeface.NORMAL);
            button.setText(entity.getName());
        } else {
            System.out.println("found wikiname for button: " + entity.getWikiName());
            button.setPaintFlags(button.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            button.setTypeface(null, Typeface.BOLD);
            button.setText(entity.getWikiName());
        }
    }

    public int getNewEntityColour() {
        int colour = -1;
        for (int i = 0; i < htmlColours.length; i++) {
            if (!usedColours.contains(i)) {
                colour = i;
                break;
            }
        }
        if (colour == -1) {
            colour = new Random().nextInt(htmlColours.length);
            activity.showToast("Reusing colours!");
        }
        return colour;

    }

    public void addEntity(int wordViewId, Entity entityAttached) {
        // check if position is already part of entity
        Map<String,Object> wordMap = data.getWords().get(wordViewId);
        int start = (Integer)wordMap.get("start");
        int length = (Integer)wordMap.get("length");
        String word = (String)wordMap.get("word");
        Entity wordEntity = (Entity)wordMap.get("entity");

        if (wordEntity != null && wordEntity != entityAttached) {
            int entityToRemove = -1;
            for (int i = 0; i < data.getNumEntities(); i++) {
                Entity entity = data.getEntity(i);
                List<Position> positions = entity.getPositions();
                int matchingPosition = -1;
                for (int j = 0; j < positions.size(); j++) {
                    Position position = positions.get(j);
                    if (position.contains(start, length)) {
                        matchingPosition = j;
                        //activity.showToast("This word is already part of an entity!");
                        //return;
                    }
                }
                if (matchingPosition > -1) {
                    positions.remove(matchingPosition);
                    wordMap.remove("entity");
                    wordEntity = null;
                }
                if (positions.size() == 0) {
                    entityToRemove = i;
                    break;
                }
            }

            if (entityToRemove > -1) {
                data.removeEntity(entityToRemove);
            }
        }

        if (entityAttached == null) {
            System.out.println("Add new entity from word button");
        } else {
            System.out.println("Extend entity with word from word button.");
        }

        if (wordEntity == null) {
            if (entityAttached == null) {
                System.out.println("create new entity: " + word);
                int colour = getNewEntityColour();
                List<Position> positions = new ArrayList<>();
                positions.add(new Position(start, length));
                data.addEntity(colour,EntityButtonProperty.NONE, positions);
                usedColours.add(colour);
            } else {
                entityAttached.addPosition(start, length, data);
            }
        } else {
            activity.showToast("This word is already part of an entity!");
        }

        for (int i = 0; i < data.getNumEntities(); i++) {
            data.getEntity(i).findName(data);
        }
    }

    public void reloadViews(boolean reloadData) {
        //if (data != null && reloadData) {
            data.createAnnotations();
            data.createWords();
        //}
        fillEntityButtonScrollView();
        fillTextView(true);
        fillWordButtonView();
    }

    public void removeWordFromPositions(int index) {
        Map<String, Object> wordMap = data.getWords().get(index);
        int start = (int) wordMap.get("start");
        int length = (int) wordMap.get("length");
        Entity entity = (Entity) wordMap.get("entity");
        if (entity == null) {
            activity.showToast("No entities to remove from this word!");
        } else {
            List<Position> newPositions = new ArrayList<>();
            for (Position position : entity.getPositions()) {
                if (!(position.start <= start && start + length <= position.start + position.length)) {
                    newPositions.add(position);
                } else {
                    System.out.println("CREATE NEW POSITION");
                    System.out.println(position.start + ", " + position.length);
                    System.out.println(start);
                    System.out.println(length);
                    if (start == position.start) {
                        System.out.println("at start");
                        //word button at beginning of position
                        int newStart = start + length;
                        int newLength = position.length - length;
                        if (newStart < data.getSentence().length() && data.getSentence().charAt(newStart) == ' ') {
                            newStart++;
                            newLength--;
                        }
                        if (newStart >= 0 && newLength > 0) {
                            newPositions.add(new Position(newStart, newLength));
                        }
                    } else if (start+length == position.start + position.length) {
                        System.out.println("at end");
                        //word button at the end of position
                        int newStart = position.start;
                        int newLength = position.length - length;

                        if (data.getSentence().charAt(newStart + newLength - 1) == ' ') {
                            newLength--;
                        }

                        if (newStart >= 0 && newLength > 0) {
                            newPositions.add(new Position(newStart, newLength));
                        }
                    } else {
                        System.out.println("in between");
                    }
                }
            }
            entity.setPositions(newPositions,data);

            wordMap.remove("entity");
            data.cleanUpEntities();
        }
        //TODO: It may not be necessary to reload everything. But it looks like sentence, entity and word view need to be reloaded.
        reloadViews(false);
    }

    private void setTextViewText(String text) {
        TextView textView = rootView.findViewById(R.id.textView);
        textView.setText(text);
    }

    public void removeEntity(int index) {
        System.out.println("removeEntity");
        data.removeEntity(index);
        System.out.println("createAnnotations");
        data.createAnnotations();
        //System.out.println("createWords");
        //data.createWords();

        reloadViews(false);
    }
}
