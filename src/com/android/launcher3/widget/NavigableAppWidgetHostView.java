/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.launcher3.widget;

import static com.android.launcher3.Utilities.dpToPx;

import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.widget.ListView;

import com.android.launcher3.R;
import com.android.launcher3.Reorderable;
import com.android.launcher3.dragndrop.DraggableView;
import com.android.launcher3.util.MultiTranslateDelegate;
import com.android.launcher3.views.ActivityContext;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Extension of AppWidgetHostView with support for controlled keyboard navigation.
 */
public abstract class NavigableAppWidgetHostView extends AppWidgetHostView
        implements DraggableView, Reorderable {

    private final MultiTranslateDelegate mTranslateDelegate = new MultiTranslateDelegate(this);

    /**
     * The scaleX and scaleY value such that the widget fits within its cellspans, scaleX = scaleY.
     */
    private float mScaleToFit = 1f;

    private float mScaleForReorderBounce = 1f;

    @ViewDebug.ExportedProperty(category = "launcher")
    private boolean mChildrenFocused;

    protected final ActivityContext mActivity;

    private boolean mDisableSetPadding = false;
    private boolean mCalendarWidget = false;

    public NavigableAppWidgetHostView(Context context) {
        super(context);
        mActivity = ActivityContext.lookupContext(context);
    }

    @Override
    public int getDescendantFocusability() {
        return mChildrenFocused ? ViewGroup.FOCUS_BEFORE_DESCENDANTS
                : ViewGroup.FOCUS_BLOCK_DESCENDANTS;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (mChildrenFocused && event.getKeyCode() == KeyEvent.KEYCODE_ESCAPE
                && event.getAction() == KeyEvent.ACTION_UP) {
            mChildrenFocused = false;
            requestFocus();
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (!mChildrenFocused && keyCode == KeyEvent.KEYCODE_ENTER) {
            event.startTracking();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (event.isTracking()) {
            if (!mChildrenFocused && keyCode == KeyEvent.KEYCODE_ENTER) {
                mChildrenFocused = true;
                ArrayList<View> focusableChildren = getFocusables(FOCUS_FORWARD);
                focusableChildren.remove(this);
                int childrenCount = focusableChildren.size();
                switch (childrenCount) {
                    case 0:
                        mChildrenFocused = false;
                        break;
                    case 1: {
                        if (shouldAllowDirectClick()) {
                            focusableChildren.get(0).performClick();
                            mChildrenFocused = false;
                            return true;
                        }
                        // continue;
                    }
                    default:
                        focusableChildren.get(0).requestFocus();
                        return true;
                }
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    /**
     * For a widget with only a single interactive element, return true if whole widget should act
     * as a single interactive element, and clicking 'enter' should activate the child element
     * directly. Otherwise clicking 'enter' will only move the focus inside the widget.
     */
    protected abstract boolean shouldAllowDirectClick();

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        if (gainFocus) {
            mChildrenFocused = false;
            dispatchChildFocus(false);
        }
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
    }

    @Override
    public void requestChildFocus(View child, View focused) {
        super.requestChildFocus(child, focused);
        dispatchChildFocus(mChildrenFocused && focused != null);
        if (focused != null) {
            focused.setFocusableInTouchMode(false);
        }
    }

    @Override
    public void clearChildFocus(View child) {
        super.clearChildFocus(child);
        dispatchChildFocus(false);
    }

    @Override
    public void setAppWidget(int appWidgetId, AppWidgetProviderInfo info) {
        // Prevent default padding being set on the view based on provider info. Launcher manages
        // its own widget spacing
        mDisableSetPadding = true;
        super.setAppWidget(appWidgetId, info);
        mDisableSetPadding = false;
        if ("Calendar schedule".equals(info.label)) {
            mCalendarWidget = true;
            int left = dpToPx(16);
            setPadding(left, 0, left/2, 0);
        } else {
            mCalendarWidget = false;
        }
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        if(mCalendarWidget) {
           updateCalendarWidget((ViewGroup) child);
        }
    }
    
    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        if (!mDisableSetPadding) {
            super.setPadding(left, top, right, bottom);
        }
    }

    @Override
    public boolean dispatchUnhandledMove(View focused, int direction) {
        return mChildrenFocused;
    }
    
// Calendar widget structure
//FrameLayout{#7f0b023b com.google.android.calendar:id/ghost_chip}
//
//    
//LinearLayout{#7f0b0636 com.google.android.calendar:id/widget_month_label}
//			TextView{#7f0b0634 com.google.android.calendar:id/widget_month}
//			TextView{#7f0b0635 com.google.android.calendar:id/widget_month_alternate}
//
//	FrameLayout{#7f0b0032 com.google.android.calendar:id/accessibility_fab_target_wrapper}
//      TextView{#7f0b0031 com.google.android.calendar:id/accessibility_fab_target}
//
//
//LinearLayout{#7f0b01e3 com.google.android.calendar:id/event_chip}
//	FrameLayout{#7f0b0630 com.google.android.calendar:id/widget_day_column}
//		ImageView{#7f0b0638 com.google.android.calendar:id/widget_today_circle}
//		TextView{#7f0b0633 com.google.android.calendar:id/widget_day_weekday}
//		FrameLayout{#7f0b0632 com.google.android.calendar:id/widget_day_month_day_wrapper}
//
//
//	FrameLayout{#7f0b0637 com.google.android.calendar:id/widget_row}
//		ImageView{#7f0b0083 com.google.android.calendar:id/agenda_item_color}
//
//			TextView{#7f0b05e0 com.google.android.calendar:id/title}
//			TextView{#7f0b062d com.google.android.calendar:id/when}
//
//
//FrameLayout{#7f0b0637 com.google.android.calendar:id/widget_row}
//				ImageView{#7f0b0124 com.google.android.calendar:id/chip_badged_icon_background}
//				
//	ImageView{#7f0b0083 com.google.android.calendar:id/agenda_item_color}
//		TextView{#7f0b05e0 com.google.android.calendar:id/title}
    
    private String widgetMonthName = "widget_month";
    private String rowColorName = "agenda_item_color";
    private String rowName = "widget_row";
    private String eventChipName = "event_chip";
    private String widgetMonthLabelName = "widget_month_label";
    private String ghostChipName = "ghost_chip";
    private String acssFabName = "accessibility_fab_target";
    private int bottomPadding = dpToPx(8);
    
    private String getFullIdName(String idName) {
        return "com.google.android.calendar:id/" + idName;
    }

    private int getIdFromName(Resources resources, String idName) {
        return resources.getIdentifier(getFullIdName(idName), null, null);
    }

    private void updateChildAlpha(View view) {
        if(view != null) {
//            System.out.println(" >>> Updating chld alpha " + view.getClass().getName());
            view.setAlpha(0.65f);
        }
    }
    
    private void updateChildView(ViewGroup mainView, ViewGroup viewGroup, HashMap<String, Integer> idMap) {
        int id = viewGroup.getId();
        if(id == idMap.get(eventChipName) || id == idMap.get(rowName)) {
            updateChildAlpha(viewGroup.findViewById(idMap.get(rowColorName)));
        } else if (id == idMap.get(widgetMonthLabelName)){
            View view = viewGroup.findViewById(idMap.get(acssFabName));
            if(view != null) {
                view.setVisibility(GONE);
                view = viewGroup.getChildAt(0);
                view.setMinimumHeight(0);
                view.setPadding(0, bottomPadding / 2, 0, bottomPadding);
            }
        } else if(id == idMap.get(ghostChipName)) {
            mainView.setOnHierarchyChangeListener(new OnHierarchyChangeListener() {
                @Override
                public void onChildViewAdded(View parent, View child) {
//                    System.out.println(">>>> nested hier update " + child.getClass().getName());
                    if (child instanceof ViewGroup) {
                        updateChildView(mainView, (ViewGroup) child, idMap);
                    }
                }

                @Override
                public void onChildViewRemoved(View parent, View child) {

                }
            });
            
        }
    }
    
    private void updateListChild(ViewGroup childView, HashMap<String, Integer> idMap) {
        if (childView.getChildCount() > 0) {
            updateChildView(childView, (ViewGroup) childView.getChildAt(0), idMap);
        }
    }
        
    
    private void updateCalendarWidget(ViewGroup child) {
        if (child.getChildCount() > 0 && child.getChildAt(0) instanceof ListView list && child.getBackground() instanceof GradientDrawable) {
            System.out.println(">>>> updating calendar widget color");
            if (child.getChildCount() >= 3) {
                child.getChildAt(2).setAlpha(0);
            }
            
            HashMap<String, Integer> map = new HashMap<>();
            Resources resources = list.getResources();
            map.put(widgetMonthName, getIdFromName(resources, widgetMonthName));
            map.put(rowColorName, getIdFromName(resources, rowColorName));
            map.put(eventChipName, getIdFromName(resources, eventChipName));
            map.put(widgetMonthLabelName, getIdFromName(resources, widgetMonthLabelName));
            map.put(ghostChipName, getIdFromName(resources, ghostChipName));
            map.put(rowName, getIdFromName(resources, rowName));
            map.put(acssFabName, getIdFromName(resources, acssFabName));
            
            child.getBackground().setAlpha(0);
            list.setDividerHeight(getResources().getDimensionPixelSize(R.dimen.all_apps_height_extra));
            list.setVerticalFadingEdgeEnabled(true);
            list.setFadingEdgeLength(list.getDividerHeight() * 4);
            list.setOnHierarchyChangeListener(new OnHierarchyChangeListener() {
                @Override
                public void onChildViewAdded(View parent, View child) {
                    updateListChild((ViewGroup) child, map);
                }

                @Override
                public void onChildViewRemoved(View parent, View child) {
                    if(child instanceof ViewGroup) {
                        ((ViewGroup) child).setOnHierarchyChangeListener(null);
                    }
                }
            });
        }
    }

    private void dispatchChildFocus(boolean childIsFocused) {
        // The host view's background changes when selected, to indicate the focus is inside.
        setSelected(childIsFocused);
    }

    private void updateScale() {
        super.setScaleX(mScaleToFit * mScaleForReorderBounce);
        super.setScaleY(mScaleToFit * mScaleForReorderBounce);
    }

    @Override
    public MultiTranslateDelegate getTranslateDelegate() {
        return mTranslateDelegate;
    }

    @Override
    public void setReorderBounceScale(float scale) {
        mScaleForReorderBounce = scale;
        updateScale();
    }

    @Override
    public float getReorderBounceScale() {
        return mScaleForReorderBounce;
    }

    public void setScaleToFit(float scale) {
        mScaleToFit = scale;
        updateScale();
    }

    public float getScaleToFit() {
        return mScaleToFit;
    }

    @Override
    public int getViewType() {
        return DRAGGABLE_WIDGET;
    }

    @Override
    public void getWorkspaceVisualDragBounds(Rect bounds) {
        int width = (int) (getMeasuredWidth() * mScaleToFit);
        int height = (int) (getMeasuredHeight() * mScaleToFit);
        bounds.set(0, 0, width, height);
    }
}
