package com.viewpager.dndp.fragment;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.viewpager.dndp.views.DNDItem;
import com.viewpager.dndp.views.IDNDPager;

import java.util.List;

public class FCollectionAdapter extends FragmentPagerAdapter {

    public static String TAG = "FCollectionAdapter";
    protected IDNDPager.AutoSwipe autoSwipe;
    protected List<DNDItem> dndItems;
    protected int pageCount = 1;
    protected View.OnClickListener btnListener;
    protected boolean isEditable = false;
    protected IDNDPager.ItemView event;
    protected String groupId = "";
    protected int rowNum, colNum;
    protected IDNDPager.OnButtonPreInit buttonPreInit;


    protected IDNDPager.OnChangeLocationListener onChangeLocationListener = view -> {
        //do nothing
    };


    /**
     * @param fm        - use support fragment manager
     * @param rowNum    - number of rows of each page
     * @param colNum    - number of columns of each page
     * @param dndItems  - DNDItems list to be applied in the view_pager
     * @param autoSwipe - callback method, use DNDUtils.defaultAutoSwipe().
     */
    public FCollectionAdapter(@NonNull FragmentManager fm, int rowNum, int colNum, List<DNDItem> dndItems, IDNDPager.AutoSwipe autoSwipe) {
        super(fm);
        this.autoSwipe = autoSwipe;
        this.dndItems = dndItems;
        this.rowNum = rowNum;
        this.colNum = colNum;
    }

    /**
     * @param fm        - use support fragment manager
     * @param rowNum    - number of rows of each page
     * @param colNum    - number of columns of each page
     * @param dndItems  - DNDItems list to be applied in the view_pager
     * @param groupId   - group id of each page
     * @param autoSwipe - callback method, use DNDUtils.defaultAutoSwipe().
     */
    public FCollectionAdapter(@NonNull FragmentManager fm, int rowNum, int colNum, List<DNDItem> dndItems, String groupId, IDNDPager.AutoSwipe autoSwipe) {
        super(fm);
        this.autoSwipe = autoSwipe;
        this.dndItems = dndItems;
        this.groupId = groupId;
        this.rowNum = rowNum;
        this.colNum = colNum;
    }

    /**
     * assign a listener from each page.
     *
     * @param btnListener
     */
    public void setOnClickBtnListener(View.OnClickListener btnListener) {
        this.btnListener = btnListener;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {

        final FPage fragment = new FPage(position, autoSwipe, rowNum, colNum, groupId, () -> isEditable);
        Bundle bundle = new Bundle();
        position++;
        bundle.putString("message", "hello from pages : " + position);
        fragment.setArguments(bundle);
        fragment.setItemList(dndItems);
        fragment.setCustomizeFragment(event);
        fragment.setOnChangeLocationListener(onChangeLocationListener);
        fragment.setOnButtonPreInit(buttonPreInit);
        fragment.setPostAction(new IDNDPager.ActionEvent() {
            @Override
            public void onExecute() {
                hasRemainingItems();
                fragment.getPager().setOnClickBtnListener(btnListener);
            }
        });

        return fragment;
    }

    /**
     * modify the button before rendering to layout
     * note: avoid directly modifying the size & background of button thru here.
     *
     * @param buttonPreInit - called on generateButton() method
     */
    public void setOnButtonPreInit(IDNDPager.OnButtonPreInit buttonPreInit) {
        this.buttonPreInit = buttonPreInit;
    }

    /**
     * @param isEditable - if true users can drag & drop the buttons inside the view_pager,
     *                    this also allow users to customize the button's properties
     */
    public void setEditable(final boolean isEditable) {
        this.isEditable = isEditable;
    }


    /**
     * By default a customize fragment is already implemented on runtime.
     * To activate, the adapter must be set to editable and the selected button must be clicked two times.
     *
     * @param event - callback method on double clicked.
     */
    public void setCustomizeFragment(IDNDPager.ItemView event) {
        this.event = event;
    }

    @Override
    public int getCount() {
        return pageCount;
    }

    /**
     * set the number of pages inside the view_pager.
     *
     * @param pageCount
     */
    public void setCount(int pageCount) {
        this.pageCount = pageCount;
        notifyDataSetChanged();
    }

    private void hasRemainingItems() {
        for (DNDItem item : dndItems) {
            if (!item.isAdded) {
                setCount(getCount() + 1);
                return;
            }
        }
    }


    /**
     * avoid fragments from not showing up when switching orientations.
     *
     * @param position
     * @return
     */
    @Override
    public long getItemId(int position) {
        return System.currentTimeMillis();
    }


    /**
     * updates the list of DNDItems and reset the page count to 1.
     * Note: the ViewPager.setAdapter(your_adapter) must be called again to apply changes.
     *
     * @param dndItems - new list of DNDItems
     */
    public void updateItemList(List<DNDItem> dndItems) {
        this.pageCount = 1;
        this.dndItems = dndItems;
    }


    /**
     * callback when draggable view's location is change or moved to another layout
     *
     * @param event
     */
    public void setOnChangeLocationListener(IDNDPager.OnChangeLocationListener event) {
        onChangeLocationListener = event;
    }
}
