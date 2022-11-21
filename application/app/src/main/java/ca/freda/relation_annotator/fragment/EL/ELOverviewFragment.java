package ca.freda.relation_annotator.fragment.EL;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ca.freda.relation_annotator.MainActivity;
import ca.freda.relation_annotator.R;
import ca.freda.relation_annotator.fragment.OverviewFragment;

public class ELOverviewFragment extends OverviewFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.el_overview_slide_page, container, false);
        super.fillRootView();
        return rootView;
    }

    protected void setVariables() {
        task = "EL";
        overviewPagerItem = 7;
    }
}