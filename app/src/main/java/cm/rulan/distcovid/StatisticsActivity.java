package cm.rulan.distcovid;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Vibrator;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import cm.rulan.distcovid.database.StatsDataDB;
import cm.rulan.distcovid.model.DistcovidModelManager;
import cm.rulan.distcovid.model.DistcovidModelObject;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StatisticsActivity extends AppCompatActivity {

    private final String TAG = "Stats";

    private LineChart graph;
    private List<Entry> entries;
    private LineDataSet lineDataSet;
    private ArrayList<ILineDataSet> iLineDataSets;
    private List<String> xaxis;

    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat sdf_time = new SimpleDateFormat("hh:mm:ss");

    private double currentClosestDist = 0;
    private StatsDataDB dbHelper;
    private SQLiteDatabase database;
    private final DistcovidModelManager manager = new DistcovidModelManager();

    private Vibrator vibrator; // to alarm user through phone vibration

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        setTransitionAnimation();

        // init views
        initViews();
    }

    private void initViews(){
        Log.i(TAG, "--- init start STATS ---->");
        graph = findViewById(R.id.chart_id);
        dbHelper = new StatsDataDB(this);
        Log.i(TAG, "--- DB initialized STATS ---->");
        database = dbHelper.getReadableDatabase();
        onCreateGraph();
        Log.i(TAG, " <--- init end STATS ----");
    }

    private void setTransitionAnimation(){
        Log.i(TAG, "init Transition STATS start ---->");
        if (Build.VERSION.SDK_INT > 20){
            Slide slide = new Slide();
            slide.setSlideEdge(Gravity.BOTTOM);
            slide.setDuration(350);
            slide.setInterpolator(new DecelerateInterpolator());
            getWindow().setExitTransition(slide);
            getWindow().setEnterTransition(slide);
        }
        Log.i(TAG, "<--- init Transition STATS end ----");
    }

    public void goBackToMainActivity(View view){
        Intent intent = new Intent(this, MainActivity.class);

        if (Build.VERSION.SDK_INT > 20){
            ActivityOptions activityOptions = ActivityOptions
                    .makeSceneTransitionAnimation(this);
            startActivity(intent, activityOptions.toBundle());
        }else {
            startActivity(intent);
        }
    }

    // Graph
    private void onDraw(List<DistcovidModelObject> modelObjects){
        Log.i(TAG, "onDraw Entry: STATS ----");
        // add the value to draw the origin axis
        entries.add(new Entry(0.0f, 0.0f));
        xaxis.add("0");

        // check if the list not null to avoid app crash
        if(modelObjects != null && modelObjects.size() > 0) {
            Log.i(TAG, "--- Object is not null STATS ----");
            for (int i = 0; i < modelObjects.size(); i++) {
                entries.add(new Entry((float) (i + 1), Double.valueOf(modelObjects.get(i).getDistance()).floatValue()));
                xaxis.add(modelObjects.get(i).getFormattedDate());
            }
        }else{Log.i(TAG, "--- onDraw object is null STATS ----");}
        Log.i(TAG, "--- onDraw : config graph ----");
            lineDataSet = new LineDataSet(entries, "Closest Distance (in meter)"); // Legend
            onConfigLineDataSet(lineDataSet);

            iLineDataSets.add(lineDataSet);
            LineData lineData = new LineData(iLineDataSets);
            graph.setData(lineData);

            // others config for graph
            graph.setTouchEnabled(true);
            graph.setPinchZoom(true);
            // then modified viewport
            graph.getXAxis().setPosition(XAxis.XAxisPosition.TOP);
            graph.getAxisRight().setEnabled(false); // don't show right YAxix
            graph.getXAxis().setDrawAxisLine(false);
            graph.getXAxis().setDrawGridLines(false);
            graph.getXAxis().setCenterAxisLabels(false);

            graph.getXAxis().setTextColor(Color.BLACK);
            graph.getXAxis().setTextSize(11f);
            graph.setDrawBorders(false);

            graph.getDescription().setText("Daily statistics of closest devices scanned");

            setGraphViewPort();
            graph.animateXY(1200, 2200);
        //}
        Log.i(TAG, "Leaving onDraw STATS ----");
    }

//    private void onUpdateGraph(DistcovidModelObject modelObject){
//        if(graph.getData() != null && graph.getData().getEntryCount() > 0){
//            Log.i(TAG, "Number of ENTRY: " + graph.getData().getEntryCount());
//            entries.add(new Entry((float) graph.getData().getEntryCount() + 1, Double.valueOf(modelObject.getDistance()).floatValue()));
//            xaxis.add(modelObject.getFormattedDate());
//            if(modelObject.getDistance() < currentClosestDist){
//                //msg_tv.setText(String.valueOf(modelObject.getDistance()));
//                //date_tv.setText(warning.getDate());
//                //hour_tv.setText(warning.getTime());
//
//                if (Build.VERSION.SDK_INT >=26){
//                    vibrator.vibrate(VibrationEffect
//                            .createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
//                }
//            }
//            lineDataSet.setValues(entries);
//
//            setGraphViewPort(); // actualize Viewport
//
//            graph.getData().notifyDataChanged();
//            graph.notifyDataSetChanged();
//
//            graph.animateY(1200);
//            //graph.animate();
//            graph.moveViewToX(4);
//
//
//        }
//    }

//    private void onWriting(double distance){
//
//        long datetime = System.currentTimeMillis();
//        dbHelper.insertValue(distance, datetime);
//        Log.d(TAG, "Value: ( "+distance+" ) saved in the DB");
//
//        DistcovidModelObject warning = new DistcovidModelObject(distance, datetime);
//        warning.setFormattedDate(sdf.format(new Date(datetime)));
//        warning.setFormattedTime(sdf_time.format(new Date(datetime)));
//
//        //onUpdateGraph(warning);
//    }

    private List<DistcovidModelObject> onRead(){
        Log.i(TAG, "-- Entry onRead---- ");
        List<DistcovidModelObject> warningList = manager.groupDailyDistance(dbHelper.getWarnings());

        if (warningList != null){
            Log.i(TAG, "--- warning is not != null: ");
            DistcovidModelObject w = manager.getClosestDistance(warningList);
            Log.i(TAG, "CONTENT of w: ["+w+"]");
            currentClosestDist = w.getDistance();

//            msg_tv.setText(String.valueOf(w.getValue()));
//            date_tv.setText(w.getDate());
//            hour_tv.setText(w.getTime());

            Log.i(TAG, "LIST CONTENT: "+warningList);

            for (DistcovidModelObject war: warningList) {
                Log.i(TAG, "id: " + war.get_id() + "  Value: " + war.getDistance() + " Date: " + war.getFormattedDate() + "  Time: " + war.getFormattedTime());
            }
        }else{
            warningList = new ArrayList<>();
            Log.i(TAG, "LIST CONTENT IST NULL: ["+warningList+"], size: "+warningList.size());
        }
        Log.i(TAG, "--- onRead end ---");
        return warningList;
    }

    private void onCreateGraph(){
        reset();
        onDraw(onRead());

        Log.d(TAG, "GraphData: " + graph.getData() + "  DatasetCount: "+ graph.getData().getDataSetCount() +
                "   Number of ENTRY: " + graph.getData().getEntryCount());
    }

    private void onConfigLineDataSet(LineDataSet set){

        set.setDrawIcons(false);
        set.enableDashedLine(10f, 8f, 1f);
        set.enableDashedHighlightLine(10f, 8f, 1f);
        set.setColor(Color.DKGRAY);
        set.setCircleColor(Color.DKGRAY);
        set.setLineWidth(1f);
        set.setCircleRadius(4f);
        set.setValueTextSize(10f);
        set.setDrawFilled(true);
        set.setFormLineWidth(1f);
        set.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 8f}, 1f));
        set.setFormSize(10f);

        if (Utils.getSDKInt() >= 21){
            Drawable drawable = ContextCompat.getDrawable(this, R.drawable.graph_background);
            lineDataSet.setFillDrawable(drawable);
        }
        else {
            lineDataSet.setFillColor(Color.DKGRAY);
        }
    }

    private void reset(){
        entries = null;
        lineDataSet = null;
        xaxis = null;
        iLineDataSets = null;

        entries = new ArrayList<>();
        xaxis = new ArrayList<>();
        iLineDataSets = new ArrayList<>();

    }

    private void setGraphViewPort(){

        graph.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xaxis));

        graph.getXAxis().setGranularity(1f);
        graph.getXAxis().setGranularityEnabled(true);

        graph.getXAxis().setAvoidFirstLastClipping(true); // to avoid that the last value always appear clipped
        graph.setVisibleXRangeMaximum(2);
    }
}