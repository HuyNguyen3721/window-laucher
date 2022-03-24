package com.viewpager.dndp.fragment;


import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.fragment.app.Fragment;

import com.viewpager.dndp.R;
import com.viewpager.dndp.views.DNDButton;
import com.viewpager.dndp.views.DNDItem;
import com.viewpager.dndp.views.DNDPager;
import com.viewpager.dndp.views.IDNDPager;
import com.viewpager.dndp.FCustomizePanel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class FPage extends Fragment {

    public static final String TAG = "FPAGE";
    protected List<DNDItem> dndItems = new ArrayList<>();
    protected int swipeBuffer = 500;
    protected static long recentSwipe = new Date().getTime();
    protected RelativeLayout rlGrid;
    protected IDNDPager.AutoSwipe autoSwipe;
    protected View leftBound, rightBound;
    protected DNDPager pager;
    protected int pageNum = -1;
    protected IDNDPager.ActionEvent postAction;
    protected IDNDPager.SettingsPreference settingsPreference;
    protected IDNDPager.ItemView event = null;
    protected String groupId = "";
    protected int rowNum = -1, colNum = -1;
    protected IDNDPager.OnChangeLocationListener onChangeLocationListener;
    protected IDNDPager.OnButtonPreInit onButtonPreInit;

    public FPage(int pageNum, IDNDPager.AutoSwipe autoSwipe, int rowNum, int colNum, String groupId, IDNDPager.SettingsPreference settingsPreference) {
        this.autoSwipe = autoSwipe;
        this.pageNum = pageNum;
        this.settingsPreference = settingsPreference;
        this.groupId = groupId;
        this.rowNum = rowNum;
        this.colNum = colNum;
    }

    /**
     * empty constructor is required when switching orientation
     */
    public FPage(){

    }

    /**
     * modify the button before rendering to layout
     * @param onButtonPreInit - called on generateButton() method
     */
    public void setOnButtonPreInit(IDNDPager.OnButtonPreInit onButtonPreInit){
        this.onButtonPreInit = onButtonPreInit;
    }

    /**
     * get the current pager of the Fragment
     * @return
     */
    public DNDPager getPager(){
        return pager;
    }

    /**
     * define the general customize fragment to all buttons inside the layout.
     * @param event
     */
    public void setCustomizeFragment(IDNDPager.ItemView event){
        this.event = event;
    }

    /**
     * set the group of DNDItems to be applied on DNDPager
     * @param dndItems
     */
    public void setItemList(List<DNDItem> dndItems) {
        this.dndItems = dndItems;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view  = inflater.inflate(R.layout.fragment_page, container, false);
        rlGrid = view.findViewById(R.id.rl_grid);
        leftBound = view.findViewById(R.id.left_bound);
        rightBound = view.findViewById(R.id.right_bound);

        init();

        return view;
    }


    private void init(){
        leftBound.setOnDragListener((view, dragEvent) -> {
            if(DragEvent.ACTION_DRAG_ENTERED == dragEvent.getAction() && recentSwipe + swipeBuffer < new Date().getTime()){
                recentSwipe =  new Date().getTime();
                autoSwipe.onSwipeLeft();
            }

            return true;
        });

        rightBound.setOnDragListener((view, dragEvent) -> {
            if(DragEvent.ACTION_DRAG_ENTERED == dragEvent.getAction() && recentSwipe + swipeBuffer < new Date().getTime()){
                recentSwipe =  new Date().getTime();
                autoSwipe.onSwipeRight();
            }

            return true;
        });



        pager = new DNDPager(rlGrid, rowNum, colNum, groupId,getContext());
        pager.setPageNum(pageNum);
        pager.setIsEditable(settingsPreference);
        pager.setOnChangeLocationListener(onChangeLocationListener);
        pager.setOnButtonPreInit(onButtonPreInit);


        pager.setOnCustomize(new IDNDPager.ItemView() {
            @Override
            public void onCustomize(DNDPager pager, View view) {
                Log.d(TAG, "onCustomize: is nulll");
                if(event == null){

                    DNDButton btn = (DNDButton) view;
                    FCustomizePanel
                            .getInstance(btn)
                            .show(getActivity().getSupportFragmentManager(),"customized");
                }else {
                    event.onCustomize(pager,view);
                }
            }
        });

        pager.render(() -> {
            pager.addButtonToLayout(dndItems);
            if(postAction != null){
                postAction.onExecute();
            }
        });
    }

    /**
     * callback when draggable view's location is change or moved to another layout
     * @param event
     */
    public void setOnChangeLocationListener(IDNDPager.OnChangeLocationListener event){
        onChangeLocationListener = event;
    }

    /**
     * execute after all views are rendered in DNDPager
     * @param postAction
     */
    public void setPostAction(IDNDPager.ActionEvent postAction){
        this.postAction = postAction;
    }
}
