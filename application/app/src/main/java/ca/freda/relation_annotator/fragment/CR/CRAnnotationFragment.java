package ca.freda.relation_annotator.fragment.CR;

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
import ca.freda.relation_annotator.listener.EntityButtonOnClickListener;
import ca.freda.relation_annotator.listener.EntityButtonOnTouchListener;
import ca.freda.relation_annotator.listener.EntityScrollviewDragEventListener;
import ca.freda.relation_annotator.listener.EntityViewLongClickListener;
import ca.freda.relation_annotator.listener.GarbageViewDragEventListener;
import ca.freda.relation_annotator.listener.WordButtonDragEventListener;
import ca.freda.relation_annotator.listener.WordButtonOnTouchListener;
import ca.freda.relation_annotator.util.Utils;

public class CRAnnotationFragment extends AnnotationFragment implements View.OnClickListener {

    private JSONObject currentServerMessage;

    private String currentDatasetName;
    private MainActivity activity;
    Button responseButton;

    private float start;

    private ViewGroup rootView;
    private Data data;
    private LinearLayout linearHorizontalScrollviewLayout;
    //private String[] htmlColours = {"#009900","#0099ff","#ff5050","#9933ff","#ff8b33","#7e33ff","#260505","#220b80","#4bde7b","#65c900","#800046","#a65c46","#c0d40d"};
    private String[] htmlColours = {"#e6194B", "#3cb44b", "#97850a", "#4363d8", "#f58231", "#911eb4", "#2c98b0", "#f032e6", "#5b9b22", "#cc6d91", "#30998e", "#9c63e1",
            "#9A6324", "#7f7b54", "#800000", "#538963", "#808000", "#694d32", "#000075", "#5a5a5a", "#000000"};
    private Set<Integer> usedColours = new HashSet<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.cr_annotation_slide_page, container, false);

        activity = (MainActivity) getActivity();

        HorizontalScrollView scrollView = rootView.findViewById(R.id.entity_scrollview);
        EntityScrollviewDragEventListener scrollviewDragEventListener = new EntityScrollviewDragEventListener(this);
        scrollView.setOnDragListener(scrollviewDragEventListener);




        TextView textView = rootView.findViewById(R.id.textView);
        textView.setOnDragListener(scrollviewDragEventListener);

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


        GarbageViewDragEventListener garbageViewDragEventListener = new GarbageViewDragEventListener(this);
        ImageButton trashButton = rootView.findViewById(R.id.trash);
        trashButton.setOnDragListener(garbageViewDragEventListener);

        rootView.findViewById(R.id.previous_button).setOnClickListener(this);
        rootView.findViewById(R.id.annotation_remove_button).setOnClickListener(this);
        rootView.findViewById(R.id.annotation_ignore_button).setOnClickListener(this);
        rootView.findViewById(R.id.reload_button).setOnClickListener(this);
        rootView.findViewById(R.id.main_button_annotation).setOnClickListener(this);


        System.out.println("on create annotation fragment.");

        return rootView;
    }

    private void setTextViewText(String text) {
        TextView textView = rootView.findViewById(R.id.textView);
        textView.setText(text);
    }


    @Override
    public void setUserVisibleHint(boolean visible) {
        super.setUserVisibleHint(visible);
        if (visible) {
            TextView relationTextView = rootView.findViewById(R.id.relation_textview);
            TextView uidTextView = rootView.findViewById(R.id.uid_textview);
            try {
                currentDatasetName = activity.comHandler.getCrDataset().getString("name");
                uidTextView.setText(activity.getUID());

                HorizontalScrollView scrollView = rootView.findViewById(R.id.response_scrollview);
                scrollView.removeAllViews();

                LinearLayout layout = new LinearLayout(getActivity());

                layout.setId(R.id.entity_scrollview_layout);
                layout.setOrientation(LinearLayout.HORIZONTAL);

                responseButton = new Button(getActivity());
                responseButton.setTypeface(null, Typeface.NORMAL);
                responseButton.setTag(0);
                SpannableString spanString = new SpannableString("Done");
                spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, spanString.length(), 0);
                responseButton.setText(spanString);
                responseButton.setAllCaps(true);
                responseButton.setSingleLine(true);
                responseButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setTextViewText("Waiting for data, could take around 30 seconds.");
                        sendSampleResponse((int)v.getTag());
                    }
                });
                layout.addView(responseButton);


                scrollView.addView(layout);

                switchButtonState(false);


                JSONObject message = new JSONObject();
                message.put("task", "CR");
                message.put("mode", 1);
                activity.comHandler.sendMessage(message);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            System.out.println("Annotation Fragment visible.");
        }
    }

    @Override
    public void onClick(View view) {
        System.out.println("onclick");
        switch (view.getId()) {
            case R.id.previous_button:
                switchButtonState(false);

                try {
                    JSONObject message = new JSONObject();
                    message.put("task", "CR");
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
                        message.put("task", "CR");
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
                activity.setPagerItem(2);
                break;
            case R.id.annotation_ignore_button:
                sendSampleResponse(-1);
                break;
            case R.id.annotation_remove_button:
                sendSampleResponse(-2);
                break;
            /*case R.id.relation_info_button:

                AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create(); //Read Update
                alertDialog.setTitle("How to annotate for this relation");
                alertDialog.setMessage("It is always possible to have either one subject and one or more objects, or one object and one or more subjects. If the response is 'no', subject and object are ignored, therefore they don't have to be annotated, but can be left as is. Only say 'yes', if there's no alternative explanation possible, i.e. if the relation likely holds, but is not explicitly mentioned, the answer has to be 'no'. There's no need to annotate any entity that cannot participate in this relation, but there's also no need to remove those that cannot. Except that broken annotation should be fixed, e.g. if a word is missing or the entity itself is useless.");
                alertDialog.setButton("Okay", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // here you can add functions
                    }
                });

                alertDialog.show();  //<-- See This!

                break;*/
            default:
                break;
        }
    }

    private void switchButtonState(boolean enabled) {
        if (responseButton != null) {
            rootView.findViewById(R.id.annotation_remove_button).setEnabled(enabled);
            rootView.findViewById(R.id.annotation_ignore_button).setEnabled(enabled);
            rootView.findViewById(R.id.main_button_annotation).setEnabled(enabled);
            rootView.findViewById(R.id.reload_button).setEnabled(enabled);
            rootView.findViewById(R.id.previous_button).setEnabled(enabled);
            responseButton.setEnabled(enabled);
        }
    }

    /**
     *
     * @param sampleObject
     * @throws JSONException
     */
    private void fillEntities(JSONObject sampleObject) throws JSONException {
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
            entities.put(entityJSON);
        }
        sampleObject.put("entities",entities);
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
            message.put("task", "CR");
            message.put("mode", 1);
            activity.comHandler.sendMessage(message);

        } catch (JSONException ex) {
            ex.printStackTrace();
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
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
            TextView relationTextView = rootView.findViewById(R.id.relation_textview);
            relationTextView.setText("Annotator: " + annotator + ", Dataset: " + currentDatasetName);

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
        reloadViews();
        switchButtonState(true);
    }

    private void fillTextView() {
        if (data == null) {
            setTextViewText("Waiting for data, could take around 30 seconds.");
            return;
        }


        //text = before + "<b><u><font color='" + colour + "'>" + mention + "</font></u></b>" + after;
        String sentence = data.getSentence();

        //sort entity positions
        List<Map<String,Object>> allPositions = new ArrayList<>();
        for (int i = 0; i < data.getNumEntities(); i++) {
            Entity entity = data.getEntity(i);
            for (Position position : entity.getPositions()) {
                int start = position.start;
                int length = position.length;
                Map<String,Object> entityPosition = new HashMap<>();
                entityPosition.put("start",start);
                entityPosition.put("length",length);
                entityPosition.put("colour",entity.getColour());
                entityPosition.put("property",entity.getProperty());
                allPositions.add(entityPosition);
            }
        }

        Collections.sort(allPositions, new Comparator<Map<String,Object>>(){

            @Override
            public int compare(Map<String,Object> p1, Map<String,Object> p2) {
                int start1 = (int)p1.get("start");
                int length1 = (int)p1.get("length");
                int start2 = (int)p2.get("start");
                int length2 = (int)p2.get("length");

                if (start1 == start2) {
                    return Integer.compare(length2, length1);
                } else {
                    return Integer.compare(start1, start2);
                }
            }

        });

        Map<Integer,List<Map<String,Object>>> allInnerPositions = new HashMap<>();
        Set<Integer> innerPositionIndices = new HashSet<>();
        for (int i = 0; i < allPositions.size() - 1; i++) {
            if (!innerPositionIndices.contains(i)) {
                Map<String, Object> outerPosition = allPositions.get(i);
                int outerStart = (int) outerPosition.get("start");
                int outerLength = (int) outerPosition.get("length");

                for (int j = i + 1; j < allPositions.size(); j++) {
                    if (!innerPositionIndices.contains(j)) {
                        Map<String, Object> innerPosition = allPositions.get(j);
                        int innerStart = (int) innerPosition.get("start");
                        int innerLength = (int) innerPosition.get("length");

                        if (innerStart >= outerStart && innerStart + innerLength <= outerStart + outerLength) {
                            System.out.println("Inner position: " + sentence.substring(innerStart, innerStart + innerLength));
                            if (!allInnerPositions.containsKey(i)) {
                                allInnerPositions.put(i, new ArrayList<Map<String, Object>>());
                            }
                            allInnerPositions.get(i).add(innerPosition);
                            innerPositionIndices.add(j);
                        }
                    }
                }
            }
        }

        for (Map.Entry<Integer, List<Map<String, Object>>> entry : allInnerPositions.entrySet()) {
            Collections.sort(entry.getValue(), new Comparator<Map<String,Object>>(){

                @Override
                public int compare(Map<String,Object> p1, Map<String,Object> p2) {
                    int start1 = (int)p1.get("start");
                    int start2 = (int)p2.get("start");

                    return Integer.compare(start1, start2);
                }

            });
        }




        for (int i = allPositions.size() - 1; i >= 0; i--) {
            if (innerPositionIndices.contains(i)) {
                continue;
            }
            int colour = (int)allPositions.get(i).get("colour");
            int start = (int)allPositions.get(i).get("start");
            int length = (int)allPositions.get(i).get("length");
            EntityButtonProperty property = (EntityButtonProperty) allPositions.get(i).get("property");

            String before = sentence.substring(0, start);
            String after = sentence.substring(start + length);
            String mention = sentence.substring(start, start + length);

            System.out.println("Outer mention: " + mention);

            if (allInnerPositions.containsKey(i)) {
                StringBuilder mentionBuilder = new StringBuilder();
                List<Map<String,Object>> innerPositions = allInnerPositions.get(i);
                int alreadyProcessedIndex = 0;
                for (int j = 0; j < innerPositions.size(); j++) {
                    int innerStart = (int) innerPositions.get(j).get("start");
                    int innerLength = (int) innerPositions.get(j).get("length");
                    String firstPart = mention.substring(alreadyProcessedIndex,innerStart - start);
                    String innerMentionPart = mention.substring(innerStart - start, innerStart - start + innerLength);
                    String secondPart = mention.substring(innerStart - start + innerLength);

                    System.out.println("first part: " + mention);
                    System.out.println("innerMentionPart: " + innerMentionPart);
                    System.out.println("secondPart: " + secondPart);



                    mentionBuilder.append("<span style=\"color: ");
                    mentionBuilder.append(htmlColours[colour]);
                    mentionBuilder.append(";\">");
                    mentionBuilder.append(firstPart);
                    mentionBuilder.append("</span>");
                    mentionBuilder.append("<span style=\"color: ");
                    mentionBuilder.append(htmlColours[colour]);
                    mentionBuilder.append("; background-color: ");
                    mentionBuilder.append(htmlColours[(int) innerPositions.get(j).get("colour")]);
                    mentionBuilder.append(";\">");
                    mentionBuilder.append(innerMentionPart);
                    mentionBuilder.append("</span>");
                    alreadyProcessedIndex = innerStart - start + innerLength;
                    if (j == innerPositions.size() - 1) {
                        mentionBuilder.append("<span style=\"color: ");
                        mentionBuilder.append(htmlColours[colour]);
                        mentionBuilder.append(";\">");
                        mentionBuilder.append(secondPart);
                        mentionBuilder.append("</span>");
                    }
                }
                mention = mentionBuilder.toString();
            } else {
                mention = "<span style=\"color: " + htmlColours[colour] + ";\">" + mention + "</span>";
            }

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
                List<Entity> entities = (List<Entity>)words.get(index).get("entities");
                if (entities != null) {
                    Entity entity = entities.get(0);
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

    public void entityButtonClicked(int index, Button button) {
        Entity entity = data.getEntity(index);
        entity.increaseProperty();
        setButtonLayout(button,entity);
        fillTextView();
        fillWordButtonView();
    }

    private void fillEntityButtonScrollView() {
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

        EntityViewLongClickListener longClickListener = new EntityViewLongClickListener();
        for (int i = 0; i < data.getNumEntities(); i++) {
            Entity entity = data.getEntity(i);
            System.out.println(entity.toString());
            Button button = new Button(getActivity());
            button.setActivated(false);
            button.setTypeface(null, Typeface.NORMAL);
            button.setTag(i);
            button.setOnTouchListener(new EntityButtonOnTouchListener(this));
            //button.setOnClickListener(new EntityButtonOnClickListener(this));
            button.setOnDragListener(new WordButtonDragEventListener(this,entity));
            //button.setOnLongClickListener(longClickListener);
            linearHorizontalScrollviewLayout.addView(button);

            setButtonLayout(button,entity);
        }

        scrollView.addView(layout);
    }

    private void setButtonLayout(Button button, Entity entity) {
        int colour = entity.getColour();
        System.out.println("colour: " + colour);
        int btnColour = new BigInteger("AA" + htmlColours[colour].substring(1), 16).intValue();
        button.setTextColor(btnColour);
        button.setPaintFlags(button.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
        button.setTypeface(null, Typeface.NORMAL);
        button.setText(entity.getName());
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
        List<Entity> entities = (List<Entity>)wordMap.get("entities");

        if (entities != null && entities.get(0) != entityAttached) {
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
                    wordMap.remove("entities");
                    entities = null;
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



        if (entities == null) {
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
            /*System.out.println("add inner position");
            Entity entity = entities.get(0);
            if (entityAttached != null) {
                if (entity == entityAttached) {
                    activity.showToast("This is the same entity!");
                } else {
                    activity.showToast("This word is already part of an entity!");
                    //entity = data.getEntity(entityId);
                    //entity.addPosition(start, length, data);
                }
            } else {
                System.out.println("create new entity: " + word);
                int colour = getNewEntityColour();
                if (colour == -1) {
                    activity.showToast("Cannot add more entities, must ignore this sentence!");
                } else {
                    List<Position> positions = new ArrayList<>();
                    positions.add(new Position(start, length));
                    data.addEntity(colour, EntityButtonProperty.NONE, positions);
                    usedColours.add(colour);
                }
            }*/
            activity.showToast("This word is already part of an entity!");
        }

        for (int i = 0; i < data.getNumEntities(); i++) {
            data.getEntity(i).findName(data);
        }

    }

    public void removeData() {
        currentServerMessage = null;
        switchButtonState(false);
        data = null;
        reloadViews();
    }

    public void reloadViews() {
        if (data != null) {
            data.createAnnotations();
            data.createWords();
        }
        fillEntityButtonScrollView();
        fillTextView();
        fillWordButtonView();
    }

    public void removeWordFromPositions(int index) {
        Map<String, Object> wordMap = data.getWords().get(index);
        int start = (int) wordMap.get("start");
        int length = (int) wordMap.get("length");
        List<Entity> entities = (List<Entity>) wordMap.get("entities");
        if (entities == null) {
            activity.showToast("No entities to remove from this word!");
        } else {
            for (Entity entity : entities) {
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
                            //word button in between, remove whole position, i.e. do nothing here
                        }
                    }
                }
                entity.setPositions(newPositions,data);
            }
            wordMap.remove("entities");
            data.cleanUpEntities();
        }
        reloadViews();
    }

    public void removeEntity(int index) {
        System.out.println("removeEntity");
        data.removeEntity(index);
        System.out.println("createAnnotations");
        data.createAnnotations();
        System.out.println("createWords");
        data.createWords();
        System.out.println("fillEntityButtonScrollView");
        fillEntityButtonScrollView();
        System.out.println("fillTextView");
        fillTextView();
        System.out.println("fillWordButtonView");
        fillWordButtonView();
    }

    public void setEntityType(String type, int index) {

    }
}
