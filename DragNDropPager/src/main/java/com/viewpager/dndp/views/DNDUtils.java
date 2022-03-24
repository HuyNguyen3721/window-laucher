package com.viewpager.dndp.views;

import androidx.viewpager.widget.ViewPager;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class DNDUtils {

    public static final String[][] COLOR_PALETTE = new String[][]{
            {"GREEN","#2ecc71"},
            {"BLUE","#3498db"},
            {"VIOLET","#9b59b6"},
            {"BLACK","#34495e"},
            {"YELLOW","#f1c40f"},
            {"ORANGE","#e67e22"},
            {"GREEN","#2ecc71"},
            {"RED","#e74c3c"},
            {"GREY","#95a5a6"}
    };


    /**
     * updates the is_added properties to false
     * @param item
     */
    public static void resetItems(List<DNDItem> item){
        for(DNDItem i : item){
            i.isAdded = false;
        }
    }

    /**
     * generates an autoswipe callback for View Pagers
     * @param viewPager
     * @return
     */
    public static IDNDPager.AutoSwipe defaultAutoSwipe(final ViewPager viewPager){
        return new IDNDPager.AutoSwipe() {
            @Override
            public void onSwipeLeft() {
                viewPager.setCurrentItem(viewPager.getCurrentItem() - 1, true);
            }

            @Override
            public void onSwipeRight() {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);

            }
        };
    }

    /**
     * quick sort, short code
     * @param dndItems
     */
    public static void sortItems(List<DNDItem> dndItems){
        Collections.sort(dndItems, new DNDItemComparator());
    }

    public static void validateProperties(List<DNDItem> dndItems){
        for(DNDItem item : dndItems){
            item.validateProperties();
        }
    }

    /**
     * sort items by page number
     */
    public static class DNDItemComparator implements Comparator<DNDItem> {

        @Override
        public int compare(DNDItem t1, DNDItem t2) {
            return t2.pageNum - t1.pageNum;
        }
    }



}
