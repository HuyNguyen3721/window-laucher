package com.viewpager.dndp.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * the DNDPager is the core class of the library
 */
public class DNDPager {
    public static final String TAG = "DNDPager";
    Context context;

    protected View.OnClickListener btnListener;
    /**
     * The number of rows & columns from each page to support.
     * When using multiple DNDPager with the same group id, ensure that
     * row_num & col_num values are the same from each class.
     */
    protected int rowNum, colNum;
    /**
     * The size of the layout, this is updated using updateLayoutSize(layout,IDNDPager)
     */
    protected double layoutWidth, layoutHeight;
    /**
     * This variables depends on row num & col num & layout size.
     */
    protected double cellWidth, cellHeight;

    /**
     * The layout to be populated.
     */
    protected RelativeLayout layout;

    /**
     * The current_drag_view is the current view being drag, this can also be pass from
     * one layout to another, the same principle applies with drop_shadow_view;
     */
    protected static View currentDragView, dropShadowView;

    /**
     * draggable views are only allowed to migrate to another layout
     * if it has the same group_id
     */
    protected String groupId;

    /**
     * The margin in width & height of a button, use for checking the sizes in buttons.
     * a percentage is ignored when an element overlaps another element.
     */
    protected double marginPercentage = 0.00;

    /**
     * tables to be added after layout load
     */
    protected List<DNDItem> dndItems = new ArrayList<>();
    /**
     * background color of the layout, since the layout's background color would be override by the grid snap view.
     */
    protected int backgroundColor = Color.WHITE;
    /**
     * grant user to drag & drop views
     */
    protected boolean editable = false,
    /**
     * verify if the user is holding the button
     */
    isHolding = false;

    /**
     * set the default invalid color, this is used when a view overlaps another view.
     */
    protected int invalidColor = Color.parseColor("#e74c3c");

    /**
     * the default height & width of pins
     */
    protected int pinHeight = 70, pinWidth = 70;

    /**
     * the distance of the pin from the view, 0 starts at edge
     */
    protected int pinDistance = 70;

    /**
     * action response when button is double tap (only applies when editable set to true)
     */
    protected IDNDPager.ItemView dbTapEvent;

    protected IDNDPager.OnChangeLocationListener onChangeLocationListener = view -> {
        //do nothing
    };

    /**
     * additional modification on buttons before rendering to layout
     */
    protected IDNDPager.OnButtonPreInit onButtonPreInit;

    /**
     * message response when updating the button's properties on setCustomize() callback method.
     */
    public enum MESSAGE {
        OUT_OF_BOUNDS,
        OVERLAPPED,
        SAVED,
    }


    protected DNDDoubleTap dndDoubleTap;

    protected static DNDPin pinLeft, pinTop, pinRight, pinBottom;

    /**
     * used for events
     */
    protected DNDPager instance;

    /**
     * @param layout items are populated in this layout.
     */
    public DNDPager(RelativeLayout layout, final int rowNum, final int colNum, String groupId, Context context) {
        this.layout = layout;
        this.context = context;
        this.rowNum = rowNum;
        this.colNum = colNum;
        this.groupId = groupId;

        dndDoubleTap = new DNDDoubleTap();
        dndDoubleTap.setMaxDuration(250);

        instance = this;
    }

    /**
     * enable user to customize the properties of a button by setting the editable from true to false.
     * setting the method isEditable as an interface can manipulate a group of DNDPager classes setting as one.
     */
    protected IDNDPager.SettingsPreference settingsPreference = () -> false;

    /**
     * page number, -1 is not assigned.
     */
    protected int page_num = -1;

    /**
     * must be called right after the constructor, renders the DNDSnapView.
     */
    public void render() {
        updateLayoutSize(layout, (width, height) -> {
            layoutHeight = height;
            layoutWidth = width;
            cellHeight = getCellSize(rowNum, height);
            cellWidth = getCellSize(colNum, width);

            layout.removeAllViews();
            generateSnapGrid();
        });
    }

    /**
     * alternative render must be called right after the constructor, renders the DNDSnapView.
     *
     * @param post_render - a callback is called after the view's are rendered.
     */
    public void render(final IDNDPager.ActionEvent post_render) {
        updateLayoutSize(layout, (width, height) -> {
            layoutHeight = height;
            layoutWidth = width;
            cellHeight = getCellSize(rowNum, height);
            cellWidth = getCellSize(colNum, width);

            layout.removeAllViews();
            generateSnapGrid();
            post_render.onExecute();
        });
    }

    /**
     * alternative render must be called right after the constructor, renders the DNDSnapView.
     *
     * @param dndItems directly add a list of DNDItems on render.
     */
    protected void render(final List<DNDItem> dndItems) {
        render(() -> addButtonToLayout(dndItems));
    }


    /**
     * populates the entire layout with {@link DNDSnapView},
     * the number varies on the number of rows & columns specified in a DNDPager.
     * It is the base listener of {@link DNDButton}
     */
    protected void generateSnapGrid() {

        for (int y = 0; y < rowNum; y++)
            for (int x = 0; x < colNum; x++) {
                DNDSnapView snap_view = new DNDSnapView(context);
                final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                        (int) cellWidth,
                        (int) cellHeight
                );
                int x_margin = (int) (x * cellWidth);
                int y_margin = (int) (y * cellHeight);
                params.setMargins(x_margin, y_margin, 0, 0);
                /**
                 guide only: creates a chess pattern in the layout.
                 */
                snap_view.setBackgroundColor(Color.TRANSPARENT);
                snap_view.setLayoutParams(params);
                snap_view.setPosition(x, y);

                snap_view.setOnDragListener((view, dragEvent) -> {
                    if (!settingsPreference.isEditable()) {
                        return false;
                    }
                    /**
                     * this variables are active in different business logic to reuse its data type.
                     */
                    ColorDrawable bgColor = (ColorDrawable) view.getBackground();
                    ViewGroup.MarginLayoutParams vlp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
                    DNDSnapView dndSnapView = (DNDSnapView) view;
                    RelativeLayout currentLayout;
                    RelativeLayout.LayoutParams cellPoint;
                    DNDButton btn = (DNDButton) currentDragView;

                    switch (dragEvent.getAction()) {
                        case DragEvent.ACTION_DRAG_STARTED:
                            currentLayout = dndSnapView.getLayout();
                            btn.getBackground().setAlpha(100);

                            if (pinLeft != null) {
                                pinLeft.setVisibility(View.INVISIBLE);
                                pinTop.setVisibility(View.INVISIBLE);
                                pinRight.setVisibility(View.INVISIBLE);
                                pinBottom.setVisibility(View.INVISIBLE);
                            }
                            break;
                        case DragEvent.ACTION_DRAG_ENTERED:

                            /**
                             * when a view enters the snap_view (cell)
                             * it creates a shadow where it will be drop when the user releases
                             * this serves as a guideline for the user.
                             *
                             */
                            dropShadowView = new View(context);
                            currentLayout = dndSnapView.getLayout();

                            RelativeLayout.LayoutParams shadowParams = setGridPosition(btn.getCellWidthRatio(), btn.getCellHeightRatio(), dndSnapView.getPositionX(), dndSnapView.getPositionY());
                            dropShadowView.setLayoutParams(shadowParams);
                            currentLayout.addView(dropShadowView);

                            //check if view is outside of bounds (layout)

                            /**
                             * check if view overlaps another view
                             */
                            cellPoint = setGridPosition(btn.getCellWidthRatio(), btn.getCellHeightRatio(), dndSnapView.getPositionX(), dndSnapView.getPositionY());
                            if (hasOverlapView(getParams(dropShadowView), btn)
                                    || vlp.leftMargin + cellPoint.width > layoutWidth
                                    || vlp.topMargin + cellPoint.height > layoutHeight) {
                                Log.d(TAG, "onDrag: element overlap ");
                            }

                            break;
                        case DragEvent.ACTION_DRAG_EXITED:
                            /**
                             * when the view exits the cell, it is returned to its default color.
                             */
                            layout.removeView(pinTop);
                            layout.removeView(pinLeft);
                            layout.removeView(pinRight);
                            layout.removeView(pinBottom);
                            break;
                        case DragEvent.ACTION_DRAG_LOCATION:
                            //do nothing

                            break;
                        case DragEvent.ACTION_DROP:
                            //check if view is outside of bounds (layout)
                            cellPoint = setGridPosition(btn.getCellWidthRatio(), btn.getCellHeightRatio(), dndSnapView.getPositionX(), dndSnapView.getPositionY());
                            if (vlp.leftMargin + cellPoint.width > layoutWidth
                                    ||
                                    vlp.topMargin + cellPoint.height > layoutHeight) {
                                return false;
                            }
                            //check if button group id is equal to layout's group id.
                            if (!groupId.equals(btn.getGroupId())) {
                                return false;
                            }
                            //check if it overlaps other views (buttons)
                            if (hasOverlapView(vlp, btn)) {

                                DNDButton current_cell = getButtonByCoordinates(dndSnapView.getPositionX(), dndSnapView.getPositionY());
                                /**
                                 * check if the cell (snap_view) is already occupied,
                                 * the getButtonByCoordinates() returns null if it is unoccupied
                                 */
                                if (current_cell != null) {
                                    /**
                                     check if both parties has the same ratio (width & height).
                                     */
                                    if (!hasTheSameRatio(current_cell, btn)) {
                                        return false;
                                    }


                                    /**
                                     * This might be confusing at first but it is simple.
                                     * When swapping views both parties switch coordinates.
                                     * Since we cannot guarantee the size of layout which may change at any point in time
                                     * a recalculation is done using setGridPosition() this gives a definite size and location for the view.
                                     * then updates their current coordinates in the layout.
                                     */

                                    //view a
                                    cellPoint = setGridPosition(btn.getLastPager(), btn.getCellWidthRatio(), btn.getCellHeightRatio(), btn.getPositionX(), btn.getPositionY());
                                    current_cell.setLayoutParams(cellPoint);
                                    current_cell.setPosition(btn);

                                    //view b
                                    cellPoint = setGridPosition(current_cell.getLastPager(), btn.getCellWidthRatio(), btn.getCellHeightRatio(), dndSnapView.getPositionX(), dndSnapView.getPositionY());
                                    btn.setLayoutParams(cellPoint);
                                    btn.setPosition(dndSnapView);

                                    /**
                                     * When both parties are in different layouts,
                                     * they switch layouts.
                                     */
                                    if (btn.getLastPager().layout != layout) {
                                        //view a
                                        layout.removeView(current_cell);
                                        btn.getLastPager().layout.addView(current_cell);
                                        current_cell.setLastPager(btn.getLastPager());
                                        //view b
                                        btn.getLastPager().layout.removeView(btn);
                                        layout.addView(btn);
                                        btn.setLastPager(instance);

                                    }
                                    /**
                                     * returns to their current states of background (opacity).
                                     */
                                    current_cell.getBackground().setAlpha(255);
                                    btn.getBackground().setAlpha(255);
                                    onChangeLocationListener.onChange(btn);
                                    onChangeLocationListener.onChange(current_cell);
                                }
                            } else {
                                /**
                                 * when cell is unoccupied, the view is able to occupy the cell
                                 * the view's coordinates are updated to snap_view's coordinates.
                                 */
//                                    view.setBackgroundColor(backgroundColor);

                                cellPoint = setGridPosition(btn.getCellWidthRatio(), btn.getCellHeightRatio(), dndSnapView.getPositionX(), dndSnapView.getPositionY());
                                btn.setLayoutParams(cellPoint);

                                btn.getBackground().setAlpha(255);

                                /**
                                 * when the unoccupied cell is located in foreign layout it is transfer to that layout.
                                 */
                                if (btn.getLastPager().layout != layout) {
                                    btn.setPosition(dndSnapView);
                                    btn.getLastPager().layout.removeView(currentDragView);
                                    btn.setLastPager(instance);
                                    layout.addView(btn);
                                    onChangeLocationListener.onChange(btn);

                                } else if (!hasTheSamePoint(btn, dndSnapView)) {
                                    btn.setPosition(dndSnapView);
                                    onChangeLocationListener.onChange(btn);
                                }

                            }
                            break;

                        case DragEvent.ACTION_DRAG_ENDED:
                            /**
                             * resets temporary changes to default.
                             */
                            btn.getBackground().setAlpha(255);
                            layout.removeView(dropShadowView);
                            isHolding = false;
                            break;

                    }
                    return true;
                });
                layout.addView(snap_view);
                Log.d(TAG, "generateSnapGrid: ");
            }
    }

    /**
     * computes the cell length base on the number of cells available in a layout_size
     *
     * @param cell_count  - the number of cells to fit in layout_size
     * @param layout_size - the length of a layout e.g height or width
     * @return the cell_size
     */
    private int getCellSize(int cell_count, double layout_size) {
        return (int) layout_size / cell_count;
    }

    /**
     * modify the button before rendering to layout
     *
     * @param button_pre_init - called on generateButton() method
     */
    public void setOnButtonPreInit(IDNDPager.OnButtonPreInit button_pre_init) {
        this.onButtonPreInit = button_pre_init;
    }

    /**
     * Listens to layout size change.
     * This method also avoid receiving 0 width & height on initial load of the layout.
     *
     * @param layout   - safely takes the layout size.
     * @param callback - called when size changes.
     */
    public void updateLayoutSize(final RelativeLayout layout, final IDNDPager callback) {

        ViewTreeObserver vto = layout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    layout.getViewTreeObserver()
                            .removeOnGlobalLayoutListener(this);
                } else {
                    layout.getViewTreeObserver()
                            .removeGlobalOnLayoutListener(this);
                }

                double width = layout.getMeasuredWidth();
                double height = layout.getMeasuredHeight();

                if (width != 0 && height != 0) {

                    callback.onSizeChange(width, height);
                }
            }
        });
    }

    public void updateLayoutSize(final IDNDPager callback) {
        updateLayoutSize(layout, callback);
    }

    /**
     * Creates a DNDButton to be viewed in the layout.
     *
     * @param width_ratio  - the ratio is base on the layout_width
     * @param height_ratio - the ratio is base on the layout_height
     * @param x            - coordinates in the layout this is base on the snap gridview (the col_num).
     * @param y            - coordinates in the layout this is base on the snap gridview (the row_num).
     */
    @SuppressLint("ClickableViewAccessibility")
    public DNDButton generateButton(final int width_ratio, final int height_ratio, final int x, final int y, String btn_text, Drawable btnbg_image, int btnbg_color, String tag) {
        final DNDButton btn = new DNDButton(context);
        if (onButtonPreInit != null) {
            onButtonPreInit.onInitialize(btn);
        }
        if (!tag.equals("")) {
            btn.setTag(tag);
        }

        btn.setOnTouchListener((v, me) -> {
            if (!settingsPreference.isEditable()) {
                return false;
            }

            if (!isHolding) {
                return false;
            }

            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(btn);
            /**
             * updates the current view being drag.
             */
            currentDragView = v;

            if (me.getAction() == MotionEvent.ACTION_UP) {
                v.performClick();

                Log.d(TAG, "onTouch: unclicked");
            }
            if (me.getAction() == MotionEvent.ACTION_DOWN) {
                Log.d(TAG, "onTouch: cliked");

            }
            if (me.getAction() == MotionEvent.ACTION_MOVE) {
                v.startDrag(null, shadowBuilder, null, 0);
            }


            return true;
        });


        btn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                isHolding = true;
                return true;
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (!settingsPreference.isEditable()) {
                    if (btnListener != null) {
                        btnListener.onClick(view);
                    }
                } else {
                    dndDoubleTap.doubleTap(view, new IDNDPager.ActionEvent() {
                        @Override
                        public void onExecute() {
                            dbTapEvent.onCustomize(instance, view);
                        }
                    });
                }
            }
        });


        btn.setText(btn_text);
        if (btnbg_image != null) {
            btn.setBackgroundImage(5, backgroundColor, btnbg_image);
        } else {
            btn.setBackgroundColor(btnbg_color);
            btn.setBorder(5, backgroundColor);
        }
        btn.setGroupId(groupId);
        btn.setLastPager(instance);

        btn.setCellWidthRatio(width_ratio);
        btn.setCellHeightRatio(height_ratio);
        btn.setPosition(x, y);
        btn.setLayoutParams(setGridPosition(width_ratio, height_ratio, x, y));

        return btn;
    }

    /**
     * set the default listener for all interactive views
     *
     * @param btn_listener - callback, to distinguish views from one another add tags on DNDItem.
     */
    public void setOnClickBtnListener(View.OnClickListener btn_listener) {
        this.btnListener = btn_listener;
    }

    /**
     * calculates the cell designated position base on the cell ratio given followed by x & y coordinates
     *
     * @param width_ratio  - cell_width
     * @param height_ratio - cell_height
     * @param x            - position in layout
     * @param y            - position in layout
     * @return LayoutParameters
     */
    public RelativeLayout.LayoutParams setGridPosition(double width_ratio, double height_ratio, int x, int y) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int) (width_ratio * cellWidth), (int) (height_ratio * cellHeight));
        params.setMargins((int) (x * cellWidth), (int) (y * cellHeight), 0, 0);
        return params;
    }

    /**
     * calculates the cell designated position base on the cell ratio given followed by x & y coordinates
     *
     * @param pager        - calculates the exact properties inside the layout, mostly used when migrating from one layout to another.
     * @param width_ratio  - cell_width
     * @param height_ratio - cell_height
     * @param x            - position in layout
     * @param y            - position in layout
     * @return LayoutParameters
     */
    public RelativeLayout.LayoutParams setGridPosition(DNDPager pager, double width_ratio, double height_ratio, int x, int y) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int) (width_ratio * pager.cellWidth), (int) (height_ratio * pager.cellHeight));
        params.setMargins((int) (x * pager.cellWidth), (int) (y * pager.cellHeight), 0, 0);
        return params;
    }

    /**
     * @param color - set the background color of the layout.
     *              Take Note: this is only applied before render()
     */
    public void setBackgroundColor(int color) {
        backgroundColor = color;
    }


    /**
     * @param color  - the color to be adjust
     * @param factor - ranges from 0.8 to 1.0f to set the contrast
     * @return the contrasted value.
     */
    private int colorContrast(int color, float factor) {
        int a = Color.alpha(color);
        int r = Math.round(Color.red(color) * factor);
        int g = Math.round(Color.green(color) * factor);
        int b = Math.round(Color.blue(color) * factor);
        return Color.argb(a,
                Math.min(r, 255),
                Math.min(g, 255),
                Math.min(b, 255));
    }

    /**
     * determines if the element overlaps to other elements
     *
     * @param btn    - takes the ratio to validate
     * @param params - new coordinates & size to validate
     * @return true if it has overlap.
     */
    private boolean hasOverlapView(ViewGroup.MarginLayoutParams params, IDNDPager.Item btn) {

        Log.d(TAG, "hasOverlapView: shrink size" + shrinkSize(params.leftMargin + cellWidth * btn.getCellWidthRatio(), marginPercentage));
        Log.d(TAG, "hasOverlapView: actual size " + params.leftMargin + cellWidth * btn.getCellWidthRatio());
        Log.d(TAG, "hasOverlapView:btn count " + getButtons().size());
        for (DNDButton b : getButtons()) {
            if (b != btn &&
                    expandSize(getParams(b).leftMargin, marginPercentage) < params.leftMargin + cellWidth * btn.getCellWidthRatio()
                    &&
                    shrinkSize(getButtonFullWidth(b), marginPercentage) > params.leftMargin
                    &&
                    expandSize(getParams(b).topMargin, marginPercentage) < params.topMargin + cellHeight * btn.getCellHeightRatio()
                    &&
                    shrinkSize(getButtonFullHeight(b), marginPercentage) > params.topMargin

            ) {
                return true;
            }
        }
        return false;
    }

    private ViewGroup.MarginLayoutParams getParams(View view) {
        return (ViewGroup.MarginLayoutParams) view.getLayoutParams();
    }

    /**
     * Takes the full width of a button base on the cell_width.
     *
     * @param btn
     * @return returns the full size
     */
    private double getButtonFullWidth(DNDButton btn) {
        return getParams(btn).leftMargin + cellWidth * btn.getCellWidthRatio();
    }

    /**
     * Takes the full height of a button base on the cell_height.
     *
     * @param btn
     * @return returns the full size
     */
    private double getButtonFullHeight(DNDButton btn) {
        return getParams(btn).topMargin + cellHeight * btn.getCellHeightRatio();
    }

    /**
     * Takes all buttons inside a layout.
     *
     * @return list of buttons
     */
    public List<DNDButton> getButtons() {
        List<DNDButton> btn = new ArrayList<>();
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            if (child instanceof DNDButton) {
                btn.add((DNDButton) child);
            }
        }
        Log.d(TAG, "getButtons: " + btn.size());
        return btn;
    }

    /**
     * Takes all snapviews inside a layout.
     *
     * @return list of snap_views
     */
    public List<DNDSnapView> getSnapViews() {
        List<DNDSnapView> btn = new ArrayList<>();
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            if (child instanceof DNDSnapView) {
                btn.add((DNDSnapView) child);
            }
        }
        Log.d(TAG, "getSnapViews: " + btn.size());
        return btn;
    }

    /**
     * shrink the size base on percentage
     *
     * @param size       - target value
     * @param percentage - works effectively with range 0.0 to 1.0f;
     * @return shrink size
     */
    public double shrinkSize(double size, double percentage) {
        Log.d(TAG, "shrinkSize: " + size + " " + percentage);
        return size - size * percentage;
    }

    /**
     * expand the size base on percentage
     *
     * @param size       - target value
     * @param percentage - works effectively with range 0.0 to 1.0f;
     * @return
     */
    public double expandSize(double size, double percentage) {
        Log.d(TAG, "shrinkSize: " + size + " " + percentage);
        return size + size * percentage;
    }

    /**
     * Checks if both parties has the same ratio
     *
     * @param btn1 to compare
     * @param btn2 to compare
     * @return true if they have the same ratio
     */
    public boolean hasTheSameRatio(DNDButton btn1, DNDButton btn2) {
        if (btn1.getCellHeightRatio() == btn2.getCellHeightRatio()
                && btn1.getCellWidthRatio() == btn2.getCellWidthRatio()) {
            return true;
        }
        return false;
    }

    /**
     * check if both coordinates hold the same positions
     *
     * @param c1 - first coordinate to compare
     * @param c2 - second coordinate to compare
     * @return true if both coordinates has the same positions.
     */
    public boolean hasTheSamePoint(IDNDPager.Coordinates c1, IDNDPager.Coordinates c2) {
        if (c1.getPositionX() != c2.getPositionX() || c1.getPositionY() != c2.getPositionY()) {
            return false;
        }
        return true;
    }

    /**
     * get button by coordinates inside the layout
     *
     * @param x
     * @param y
     * @return the button that has the coordinates
     */
    public DNDButton getButtonByCoordinates(int x, int y) {
        for (DNDButton b : getButtons()) {
            if (b.getPositionX() == x && b.getPositionY() == y) {
                return b;
            }
        }
        return null;
    }

    /**
     * @return an integer of color
     */
    public int getInvalidColor() {
        return invalidColor;
    }

    /**
     * changes the shadow of draggable view when dragged in an obstructed place.
     *
     * @param invalid_color
     */
    public void setInvalidColor(int invalid_color) {
        this.invalidColor = invalid_color;
    }

    /**
     * Manipulate the button's properties inside the DNDPager,
     * a callback is called only if the DNDPager is set to editable & double clicked.
     *
     * @param view
     */
    public void setOnCustomize(IDNDPager.ItemView view) {
        dbTapEvent = view;
    }

    /**
     * check the buttons coordinates & ratio
     *
     * @param btn to validate
     * @return response
     */
    public MESSAGE checkItem(ViewGroup.MarginLayoutParams params, DNDButton btn) {
        if (hasOverlapView(params, btn)) {
            return MESSAGE.OVERLAPPED;
        } else if (params.leftMargin + params.width > layoutWidth
                ||
                params.topMargin + params.height > layoutHeight) {
            return MESSAGE.OUT_OF_BOUNDS;
        } else {
            return MESSAGE.SAVED;
        }
    }

    /**
     * @param btn      target button to add pins
     * @param location {left,right,top,bottom} pins are added to button
     */
    private DNDPin generatePin(DNDButton btn, String location) {
        DNDPin.DRAG_DIRECTION direction = null;
        ViewGroup.MarginLayoutParams btn_params = getParams(btn);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(pinWidth, pinHeight);
        int btn_width = (int) cellWidth * btn.getCellWidthRatio();
        int btn_height = (int) cellHeight * btn.getCellHeightRatio();

        int x_margin = 0, y_margin = 0;
        switch (location) {
            case "left":
                x_margin -= pinDistance;
                y_margin += (btn_height / 2) - pinHeight / 2;
                direction = DNDPin.DRAG_DIRECTION.DRAG_HORIZONTAL;
                break;
            case "top":
                x_margin += (btn_width / 2) - pinWidth / 2;
                y_margin -= pinDistance;
                direction = DNDPin.DRAG_DIRECTION.DRAG_VERTICAL;
                break;
            case "right":
                x_margin += btn_width;
                y_margin += (btn_height / 2) - pinHeight / 2;
                direction = DNDPin.DRAG_DIRECTION.DRAG_HORIZONTAL;
                break;
            case "bottom":
                x_margin += (btn_width / 2) - pinWidth / 2;
                y_margin += btn_height;
                direction = DNDPin.DRAG_DIRECTION.DRAG_VERTICAL;
                break;

        }
        DNDPin pin = new DNDPin(direction, context);
        pin.setBackgroundColor(Color.RED);
        int left_margin = btn_params.leftMargin + x_margin;
        int top_margin = btn_params.topMargin + y_margin;
        params.setMargins(left_margin, top_margin, 0, 0);
        pin.setLayoutParams(params);
        return pin;
    }

    /**
     * Add a group of DNDItems to a layout
     *
     * @param items - list of DNDItems
     */
    public void addButtonToLayout(List<DNDItem> items) {
        DNDUtils.sortItems(items);
        for (DNDItem item : items) {
            addButtonToLayout(item);
        }
    }

    /**
     * add items to the DNDPager's layout.
     *
     * @param item to be added.
     * @return true if item is added in layout else it does not have enough space.
     */
    public boolean addButtonToLayout(DNDItem item) {

        if (item.isAdded) {
            return true;
        }

        if (item.pageNum != -1 && page_num != item.pageNum) {
            return false;
        }
        if (item.x > -1 && item.y > -1) {
            item.dndButton = generateButton(item.cellWidthRatio, item.cellHeightRatio, item.x, item.y, item.text, item.backgroundImage, item.backgroundColor, item.tag);
            layout.addView(
                    item.dndButton
            );
            item.isAdded = true;
            return true;
        }


        for (int y = 0; y < rowNum; y++) {
            for (int x = 0; x < colNum; x++) {
                Log.d(TAG, "addButtonToLayout: " + item.cellWidthRatio + "-" + item.cellHeightRatio + "-" + x + "-" + y + "-" + item.text + "-" + rowNum + " " + colNum + " " + (x < colNum));
                DNDButton btn = getButtonByCoordinates(x, y);
                if (btn != null) {
                    continue;
                }
                RelativeLayout.LayoutParams params = setGridPosition(item.cellWidthRatio, item.cellHeightRatio, x, y);
                DNDButton temp_btn = new DNDButton(context);
                temp_btn.setCellWidthRatio(item.cellWidthRatio);
                temp_btn.setCellHeightRatio(item.cellHeightRatio);

                if (hasOverlapView(params, temp_btn)) {
                    continue;
                }

                dndItems.add(item);
                item.x = x;
                item.y = y;
                item.dndButton = generateButton(item.cellWidthRatio, item.cellHeightRatio, item.x, item.y, item.text, item.backgroundImage, item.backgroundColor, item.tag);
                layout.addView(
                        item.dndButton
                );
                item.pageNum = page_num;
                item.isAdded = true;
                Log.d(TAG, "addButtonToLayout: -------------------" + x + " " + y + " ");
                return true;
            }
        }
        return false;
    }

    /**
     * Callback used for advance validations
     *
     * @param settingsPreference
     */
    public void setIsEditable(IDNDPager.SettingsPreference settingsPreference) {
        this.settingsPreference = settingsPreference;
    }

    /**
     * directly change the editable
     *
     * @param editable
     */
    public void setIsEditable(final boolean editable) {
        this.settingsPreference = new IDNDPager.SettingsPreference() {
            @Override
            public boolean isEditable() {
                return editable;
            }
        };
    }

    /**
     * remove all buttons inside the layout
     */
    public void clearInteractiveViews() {
        for (DNDButton btn : getButtons()) {
            layout.removeView(btn);
        }
    }

    /**
     * reset is_added from true to false, applied to all items
     *
     * @param item_list
     */
    public void clearCache(List<DNDItem> item_list) {
        DNDUtils.resetItems(item_list);
    }

    /**
     * update the items inside the layout
     *
     * @param list_item
     */
    public void updateButtons(final List<DNDItem> list_item) {

        if (layoutHeight == 0 && layoutWidth == 0) {
            updateLayoutSize(new IDNDPager() {
                @Override
                public void onSizeChange(double width, double height) {
                    clearCache(list_item);
                    clearInteractiveViews();
                    addButtonToLayout(list_item);
                }
            });
        } else {
            clearCache(list_item);
            clearInteractiveViews();
            addButtonToLayout(list_item);
        }


    }


    /**
     * get the current page number
     *
     * @return
     */
    public int getPageNum() {
        return page_num;
    }


    /**
     * set the page number, this must be unique if applied in a ViewPager or to a group of group_id
     *
     * @return
     */
    public void setPageNum(int page_num) {
        this.page_num = page_num;
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
