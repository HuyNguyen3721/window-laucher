package com.viewpager.dndp.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;

import androidx.appcompat.widget.AppCompatButton;

public class DNDButton extends AppCompatButton implements IDNDPager.Item {
    protected int cellWidth, cellHeight;

    protected String groupId;

    protected DNDPager pager;

    protected GradientDrawable gradientDrawable;

    protected int color;

    protected int x, y;

    protected Drawable bgImage = null;

    public DNDButton(Context context) {
        super(context);
        gradientDrawable = new GradientDrawable();
    }

    /**
     * get the current width of button in ratio.
     * ratio is used instead of actual px size since the parent layout's size vary.
     * The ratio is base on the layout's number of columns defined in the {@link DNDPager} class,
     * it must be greater than or equal to one and less than or equal to the number of the parent layout's number of columns.
     *
     * @return
     */
    public int getCellWidthRatio() {
        return cellWidth;
    }

    /**
     * set the current width of button in ratio.
     * ratio is used instead of actual px size since the parent layout's size vary.
     * The ratio is base on the layout's number of columns defined in the {@link DNDPager} class,
     * it must be greater than or equal to one and less than or equal to the number of the parent layout's number of columns.
     *
     * @return
     */
    public void setCellWidthRatio(int cellWidth) {
        this.cellWidth = cellWidth;
    }

    /**
     * get the current width of button in ratio.
     * ratio is used instead of actual px size since the parent layout's size vary.
     * The ratio is base on the layout's number of rows defined in the {@link DNDPager} class,
     * it must be greater than or equal to one and less than or equal to the number of the parent layout's number of rowss.
     *
     * @return
     */
    public int getCellHeightRatio() {
        return cellHeight;
    }

    /**
     * set the current width of button in ratio.
     * ratio is used instead of actual px size since the parent layout's size vary.
     * The ratio is base on the layout's number of rows defined in the {@link DNDPager} class,
     * it must be greater than or equal to one and less than or equal to the number of the parent layout's number of rowss.
     *
     * @return
     */
    public void setCellHeightRatio(int cellHeight) {
        this.cellHeight = cellHeight;
    }

    /**
     * set the last DNDPager's origin
     * DNDButtons can migrate from one layout to another with the same {@link DNDButton#groupId} by dragging the view,
     * by doing so DNDPager's can identify if it came from a foreign layout,
     * the button is then removed from its origin and then added to the new layout and update it's new origin.
     * If the button is native to the layout then it's coordinates are updated.
     *
     * @param pager - recent DNDPager class i.e. origin
     */
    public void setLastPager(DNDPager pager) {
        this.pager = pager;
    }

    /**
     * get the last DNDPager's origin
     * DNDButtons can migrate from one layout to another with the same {@link DNDButton#groupId} by dragging the view,
     * by doing so DNDPager's can identify if it came from a foreign layout,
     * the button is then removed from its origin and then added to the new layout.
     * If the button is native to the layout then it's coordinates are updated.
     */
    public DNDPager getLastPager() {
        return pager;
    }

    /**
     * Buttons inside the same layout carries the same group_id,
     * applied when dragging a view to another layout with the same group_id.
     * If a button with a different group_id is migrated to another layout it won't be accepted and force back to it's origin.
     *
     * @return the group_id of DNDButton.
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * set the group_id of a button.
     * Buttons inside the same layout carries the same group_id,
     * applied when dragging a view to another layout with the same group_id.
     * If a button with a different group_id is migrated to another layout it won't be accepted and force back to it's origin.
     *
     * @param groupId - defined by DNDPager upon adding a button.
     */
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    /**
     * set the background color of the button
     *
     * @param color - accepts integer as color. {@link Color#parseColor(String) can also be used to apply hex}
     */
    @Override
    public void setBackgroundColor(int color) {
        this.color = color;
        gradientDrawable.setColor(color);
        setBackground(gradientDrawable);
    }

    /**
     * @return - the background color of the button.
     */
    public int getColor() {
        return color;
    }

    /**
     * set the corners of the buttons by radius
     *
     * @param radius
     */
    public void setCornerRadius(float radius) {
        gradientDrawable.setCornerRadius(radius);
        setBackground(gradientDrawable);
    }

    /**
     * set the border of the button
     *
     * @param border set the width
     * @param color  set the border color
     */
    public void setBorder(int border, int color) {
        gradientDrawable.setStroke(border, color);
        setBackground(gradientDrawable);
    }


    /**
     * set the background image the button.
     *
     * @param border
     * @param image
     */
    public void setBackgroundImage(int border, int borderColor, Drawable image) {
        GradientDrawable frame = gradientDrawable;
        frame.setStroke(border, borderColor);
        Drawable[] layers = {image, frame};
        LayerDrawable result = new LayerDrawable(layers);
        bgImage = image;
        setBackground(result);
    }

    /**
     * get the background image of the button
     *
     * @return
     */
    public Drawable getBackgroundImage() {
        return bgImage;
    }

    /**
     * set the current coordinates of the button
     * Note: this does not directly apply when already added to DNDPager, it only serves as an information.
     *
     * @param x - x coordinate inside the layout
     * @param y - y coordinate inside the layout
     */
    @Override
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * set the current coordinates of the button
     * Note: this does not directly apply when already added to DNDPager, it only serves as an information.
     *
     * @param coordinates - interface can be used to specify the coordinates inside the layout
     */
    @Override
    public void setPosition(IDNDPager.Coordinates coordinates) {
        x = coordinates.getPositionX();
        y = coordinates.getPositionY();
    }

    /**
     * get the x position of the button inside the layout
     *
     * @return
     */
    @Override
    public int getPositionX() {
        return x;
    }

    /**
     * get the y position of the button inside the layout
     *
     * @return
     */
    @Override
    public int getPositionY() {
        return y;
    }


}
