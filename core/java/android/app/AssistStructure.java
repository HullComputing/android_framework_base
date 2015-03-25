/*
 * Copyright (C) 2015 The Android Open Source Project
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

package android.app;

import android.content.ComponentName;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.PooledStringReader;
import android.os.PooledStringWriter;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewAssistStructure;
import android.view.ViewGroup;
import android.view.ViewRootImpl;
import android.view.WindowManagerGlobal;
import android.widget.Checkable;

import java.util.ArrayList;

/**
 * Assist data automatically created by the platform's implementation
 * of {@link Activity#onProvideAssistData}.  Retrieve it from the assist
 * data with {@link #getAssistStructure(android.os.Bundle)}.
 */
final public class AssistStructure implements Parcelable {
    static final String TAG = "AssistStructure";

    /**
     * Key name this data structure is stored in the Bundle generated by
     * {@link Activity#onProvideAssistData}.
     */
    public static final String ASSIST_KEY = "android:assist_structure";

    final ComponentName mActivityComponent;

    final ArrayList<WindowNode> mWindowNodes = new ArrayList<>();

    ViewAssistStructureImpl mTmpViewAssistStructureImpl = new ViewAssistStructureImpl();
    Bundle mTmpExtras = new Bundle();

    final static class ViewAssistStructureImpl extends ViewAssistStructure {
        CharSequence mText;
        int mTextSelectionStart = -1;
        int mTextSelectionEnd = -1;
        int mTextColor = ViewNode.TEXT_COLOR_UNDEFINED;
        int mTextBackgroundColor = ViewNode.TEXT_COLOR_UNDEFINED;
        float mTextSize = 0;
        int mTextStyle = 0;
        CharSequence mHint;

        @Override
        public void setText(CharSequence text) {
            mText = text;
            mTextSelectionStart = mTextSelectionEnd = -1;
        }

        @Override
        public void setText(CharSequence text, int selectionStart, int selectionEnd) {
            mText = text;
            mTextSelectionStart = selectionStart;
            mTextSelectionEnd = selectionEnd;
        }

        @Override
        public void setTextPaint(TextPaint paint) {
            mTextColor = paint.getColor();
            mTextBackgroundColor = paint.bgColor;
            mTextSize = paint.getTextSize();
            mTextStyle = 0;
            Typeface tf = paint.getTypeface();
            if (tf != null) {
                if (tf.isBold()) {
                    mTextStyle |= ViewNode.TEXT_STYLE_BOLD;
                }
                if (tf.isItalic()) {
                    mTextStyle |= ViewNode.TEXT_STYLE_ITALIC;
                }
            }
            int pflags = paint.getFlags();
            if ((pflags& Paint.FAKE_BOLD_TEXT_FLAG) != 0) {
                mTextStyle |= ViewNode.TEXT_STYLE_BOLD;
            }
            if ((pflags& Paint.UNDERLINE_TEXT_FLAG) != 0) {
                mTextStyle |= ViewNode.TEXT_STYLE_UNDERLINE;
            }
            if ((pflags& Paint.STRIKE_THRU_TEXT_FLAG) != 0) {
                mTextStyle |= ViewNode.TEXT_STYLE_STRIKE_THRU;
            }
        }

        @Override
        public void setHint(CharSequence hint) {
            mHint = hint;
        }

        @Override
        public CharSequence getText() {
            return mText;
        }

        @Override
        public int getTextSelectionStart() {
            return mTextSelectionStart;
        }

        @Override
        public int getTextSelectionEnd() {
            return mTextSelectionEnd;
        }

        @Override
        public CharSequence getHint() {
            return mHint;
        }
    }

    final static class ViewNodeTextImpl {
        final CharSequence mText;
        final int mTextSelectionStart;
        final int mTextSelectionEnd;
        int mTextColor;
        int mTextBackgroundColor;
        float mTextSize;
        int mTextStyle;
        final String mHint;

        ViewNodeTextImpl(ViewAssistStructureImpl data) {
            mText = data.mText;
            mTextSelectionStart = data.mTextSelectionStart;
            mTextSelectionEnd = data.mTextSelectionEnd;
            mTextColor = data.mTextColor;
            mTextBackgroundColor = data.mTextBackgroundColor;
            mTextSize = data.mTextSize;
            mTextStyle = data.mTextStyle;
            mHint = data.mHint != null ? data.mHint.toString() : null;
        }

        ViewNodeTextImpl(Parcel in) {
            mText = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
            mTextSelectionStart = in.readInt();
            mTextSelectionEnd = in.readInt();
            mTextColor = in.readInt();
            mTextBackgroundColor = in.readInt();
            mTextSize = in.readFloat();
            mTextStyle = in.readInt();
            mHint = in.readString();
        }

        void writeToParcel(Parcel out) {
            TextUtils.writeToParcel(mText, out, 0);
            out.writeInt(mTextSelectionStart);
            out.writeInt(mTextSelectionEnd);
            out.writeInt(mTextColor);
            out.writeInt(mTextBackgroundColor);
            out.writeFloat(mTextSize);
            out.writeInt(mTextStyle);
            out.writeString(mHint);
        }
    }

    /**
     * Describes a window in the assist data.
     */
    static public class WindowNode {
        final int mX;
        final int mY;
        final int mWidth;
        final int mHeight;
        final CharSequence mTitle;
        final ViewNode mRoot;

        WindowNode(AssistStructure assist, ViewRootImpl root) {
            View view = root.getView();
            Rect rect = new Rect();
            view.getBoundsOnScreen(rect);
            mX = rect.left - view.getLeft();
            mY = rect.top - view.getTop();
            mWidth = rect.width();
            mHeight = rect.height();
            mTitle = root.getTitle();
            mRoot = new ViewNode(assist, view);
        }

        WindowNode(Parcel in, PooledStringReader preader) {
            mX = in.readInt();
            mY = in.readInt();
            mWidth = in.readInt();
            mHeight = in.readInt();
            mTitle = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
            mRoot = new ViewNode(in, preader);
        }

        void writeToParcel(Parcel out, PooledStringWriter pwriter) {
            out.writeInt(mX);
            out.writeInt(mY);
            out.writeInt(mWidth);
            out.writeInt(mHeight);
            TextUtils.writeToParcel(mTitle, out, 0);
            mRoot.writeToParcel(out, pwriter);
        }

        public int getLeft() {
            return mX;
        }

        public int getTop() {
            return mY;
        }

        public int getWidth() {
            return mWidth;
        }

        public int getHeight() {
            return mHeight;
        }

        public CharSequence getTitle() {
            return mTitle;
        }

        public ViewNode getRootViewNode() {
            return mRoot;
        }
    }

    /**
     * Describes a single view in the assist data.
     */
    static public class ViewNode {
        /**
         * Magic value for text color that has not been defined, which is very unlikely
         * to be confused with a real text color.
         */
        public static final int TEXT_COLOR_UNDEFINED = 1;

        public static final int TEXT_STYLE_BOLD = 1<<0;
        public static final int TEXT_STYLE_ITALIC = 1<<1;
        public static final int TEXT_STYLE_UNDERLINE = 1<<2;
        public static final int TEXT_STYLE_STRIKE_THRU = 1<<3;

        final int mId;
        final String mIdPackage;
        final String mIdType;
        final String mIdEntry;
        final int mX;
        final int mY;
        final int mScrollX;
        final int mScrollY;
        final int mWidth;
        final int mHeight;

        static final int FLAGS_DISABLED = 0x00000001;
        static final int FLAGS_VISIBILITY_MASK = View.VISIBLE|View.INVISIBLE|View.GONE;
        static final int FLAGS_FOCUSABLE = 0x00000010;
        static final int FLAGS_FOCUSED = 0x00000020;
        static final int FLAGS_ACCESSIBILITY_FOCUSED = 0x04000000;
        static final int FLAGS_SELECTED = 0x00000040;
        static final int FLAGS_ACTIVATED = 0x40000000;
        static final int FLAGS_CHECKABLE = 0x00000100;
        static final int FLAGS_CHECKED = 0x00000200;
        static final int FLAGS_CLICKABLE = 0x00004000;
        static final int FLAGS_LONG_CLICKABLE = 0x00200000;

        final int mFlags;

        final String mClassName;
        final CharSequence mContentDescription;

        final ViewNodeTextImpl mText;
        final Bundle mExtras;

        final ViewNode[] mChildren;

        ViewNode(AssistStructure assistStructure, View view) {
            mId = view.getId();
            if (mId > 0 && (mId&0xff000000) != 0 && (mId&0x00ff0000) != 0
                    && (mId&0x0000ffff) != 0) {
                String pkg, type, entry;
                try {
                    Resources res = view.getResources();
                    entry = res.getResourceEntryName(mId);
                    type = res.getResourceTypeName(mId);
                    pkg = res.getResourcePackageName(mId);
                } catch (Resources.NotFoundException e) {
                    entry = type = pkg = null;
                }
                mIdPackage = pkg;
                mIdType = type;
                mIdEntry = entry;
            } else {
                mIdPackage = mIdType = mIdEntry = null;
            }
            mX = view.getLeft();
            mY = view.getTop();
            mScrollX = view.getScrollX();
            mScrollY = view.getScrollY();
            mWidth = view.getWidth();
            mHeight = view.getHeight();
            int flags = view.getVisibility();
            if (!view.isEnabled()) {
                flags |= FLAGS_DISABLED;
            }
            if (!view.isClickable()) {
                flags |= FLAGS_CLICKABLE;
            }
            if (!view.isFocusable()) {
                flags |= FLAGS_FOCUSABLE;
            }
            if (!view.isFocused()) {
                flags |= FLAGS_FOCUSED;
            }
            if (!view.isAccessibilityFocused()) {
                flags |= FLAGS_ACCESSIBILITY_FOCUSED;
            }
            if (!view.isSelected()) {
                flags |= FLAGS_SELECTED;
            }
            if (!view.isActivated()) {
                flags |= FLAGS_ACTIVATED;
            }
            if (!view.isLongClickable()) {
                flags |= FLAGS_LONG_CLICKABLE;
            }
            if (view instanceof Checkable) {
                flags |= FLAGS_CHECKABLE;
                if (((Checkable)view).isChecked()) {
                    flags |= FLAGS_CHECKED;
                }
            }
            mFlags = flags;
            mClassName = view.getAccessibilityClassName().toString();
            mContentDescription = view.getContentDescription();
            final ViewAssistStructureImpl viewData = assistStructure.mTmpViewAssistStructureImpl;
            final Bundle extras = assistStructure.mTmpExtras;
            view.onProvideAssistStructure(viewData, extras);
            if (viewData.mText != null || viewData.mHint != null) {
                mText = new ViewNodeTextImpl(viewData);
                assistStructure.mTmpViewAssistStructureImpl = new ViewAssistStructureImpl();
            } else {
                mText = null;
            }
            if (!extras.isEmpty()) {
                mExtras = extras;
                assistStructure.mTmpExtras = new Bundle();
            } else {
                mExtras = null;
            }
            if (view instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup)view;
                final int NCHILDREN = vg.getChildCount();
                if (NCHILDREN > 0) {
                    mChildren = new ViewNode[NCHILDREN];
                    for (int i=0; i<NCHILDREN; i++) {
                        mChildren[i] = new ViewNode(assistStructure, vg.getChildAt(i));
                    }
                } else {
                    mChildren = null;
                }
            } else {
                mChildren = null;
            }
        }

        ViewNode(Parcel in, PooledStringReader preader) {
            mId = in.readInt();
            if (mId != 0) {
                mIdEntry = preader.readString();
                if (mIdEntry != null) {
                    mIdType = preader.readString();
                    mIdPackage = preader.readString();
                } else {
                    mIdPackage = mIdType = null;
                }
            } else {
                mIdPackage = mIdType = mIdEntry = null;
            }
            mX = in.readInt();
            mY = in.readInt();
            mScrollX = in.readInt();
            mScrollY = in.readInt();
            mWidth = in.readInt();
            mHeight = in.readInt();
            mFlags = in.readInt();
            mClassName = preader.readString();
            mContentDescription = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
            if (in.readInt() != 0) {
                mText = new ViewNodeTextImpl(in);
            } else {
                mText = null;
            }
            mExtras = in.readBundle();
            final int NCHILDREN = in.readInt();
            if (NCHILDREN > 0) {
                mChildren = new ViewNode[NCHILDREN];
                for (int i=0; i<NCHILDREN; i++) {
                    mChildren[i] = new ViewNode(in, preader);
                }
            } else {
                mChildren = null;
            }
        }

        void writeToParcel(Parcel out, PooledStringWriter pwriter) {
            out.writeInt(mId);
            if (mId != 0) {
                pwriter.writeString(mIdEntry);
                if (mIdEntry != null) {
                    pwriter.writeString(mIdType);
                    pwriter.writeString(mIdPackage);
                }
            }
            out.writeInt(mX);
            out.writeInt(mY);
            out.writeInt(mScrollX);
            out.writeInt(mScrollY);
            out.writeInt(mWidth);
            out.writeInt(mHeight);
            out.writeInt(mFlags);
            pwriter.writeString(mClassName);
            TextUtils.writeToParcel(mContentDescription, out, 0);
            if (mText != null) {
                out.writeInt(1);
                mText.writeToParcel(out);
            } else {
                out.writeInt(0);
            }
            out.writeBundle(mExtras);
            if (mChildren != null) {
                final int NCHILDREN = mChildren.length;
                out.writeInt(NCHILDREN);
                for (int i=0; i<NCHILDREN; i++) {
                    mChildren[i].writeToParcel(out, pwriter);
                }
            } else {
                out.writeInt(0);
            }
        }

        public int getId() {
            return mId;
        }

        public String getIdPackage() {
            return mIdPackage;
        }

        public String getIdType() {
            return mIdType;
        }

        public String getIdEntry() {
            return mIdEntry;
        }

        public int getLeft() {
            return mX;
        }

        public int getTop() {
            return mY;
        }

        public int getScrollX() {
            return mScrollX;
        }

        public int getScrollY() {
            return mScrollY;
        }

        public int getWidth() {
            return mWidth;
        }

        public int getHeight() {
            return mHeight;
        }

        public int getVisibility() {
            return mFlags&ViewNode.FLAGS_VISIBILITY_MASK;
        }

        public boolean isEnabled() {
            return (mFlags&ViewNode.FLAGS_DISABLED) == 0;
        }

        public boolean isClickable() {
            return (mFlags&ViewNode.FLAGS_CLICKABLE) != 0;
        }

        public boolean isFocusable() {
            return (mFlags&ViewNode.FLAGS_FOCUSABLE) != 0;
        }

        public boolean isFocused() {
            return (mFlags&ViewNode.FLAGS_FOCUSED) != 0;
        }

        public boolean isAccessibilityFocused() {
            return (mFlags&ViewNode.FLAGS_ACCESSIBILITY_FOCUSED) != 0;
        }

        public boolean isCheckable() {
            return (mFlags&ViewNode.FLAGS_CHECKABLE) != 0;
        }

        public boolean isChecked() {
            return (mFlags&ViewNode.FLAGS_CHECKED) != 0;
        }

        public boolean isSelected() {
            return (mFlags&ViewNode.FLAGS_SELECTED) != 0;
        }

        public boolean isActivated() {
            return (mFlags&ViewNode.FLAGS_ACTIVATED) != 0;
        }

        public boolean isLongClickable() {
            return (mFlags&ViewNode.FLAGS_LONG_CLICKABLE) != 0;
        }

        public String getClassName() {
            return mClassName;
        }

        public CharSequence getContentDescription() {
            return mContentDescription;
        }

        public CharSequence getText() {
            return mText != null ? mText.mText : null;
        }

        public int getTextSelectionStart() {
            return mText != null ? mText.mTextSelectionStart : -1;
        }

        public int getTextSelectionEnd() {
            return mText != null ? mText.mTextSelectionEnd : -1;
        }

        public int getTextColor() {
            return mText != null ? mText.mTextColor : TEXT_COLOR_UNDEFINED;
        }

        public int getTextBackgroundColor() {
            return mText != null ? mText.mTextBackgroundColor : TEXT_COLOR_UNDEFINED;
        }

        public float getTextSize() {
            return mText != null ? mText.mTextSize : 0;
        }

        public int getTextStyle() {
            return mText != null ? mText.mTextStyle : 0;
        }

        public String getHint() {
            return mText != null ? mText.mHint : null;
        }

        public Bundle getExtras() {
            return mExtras;
        }

        public int getChildCount() {
            return mChildren != null ? mChildren.length : 0;
        }

        public ViewNode getChildAt(int index) {
            return mChildren[index];
        }
    }

    AssistStructure(Activity activity) {
        mActivityComponent = activity.getComponentName();
        ArrayList<ViewRootImpl> views = WindowManagerGlobal.getInstance().getRootViews(
                activity.getActivityToken());
        for (int i=0; i<views.size(); i++) {
            ViewRootImpl root = views.get(i);
            mWindowNodes.add(new WindowNode(this, root));
        }
    }

    AssistStructure(Parcel in) {
        PooledStringReader preader = new PooledStringReader(in);
        mActivityComponent = ComponentName.readFromParcel(in);
        final int N = in.readInt();
        for (int i=0; i<N; i++) {
            mWindowNodes.add(new WindowNode(in, preader));
        }
        //dump();
    }

    /** @hide */
    public void dump() {
        Log.i(TAG, "Activity: " + mActivityComponent.flattenToShortString());
        final int N = getWindowNodeCount();
        for (int i=0; i<N; i++) {
            WindowNode node = getWindowNodeAt(i);
            Log.i(TAG, "Window #" + i + " [" + node.getLeft() + "," + node.getTop()
                    + " " + node.getWidth() + "x" + node.getHeight() + "]" + " " + node.getTitle());
            dump("  ", node.getRootViewNode());
        }
    }

    void dump(String prefix, ViewNode node) {
        Log.i(TAG, prefix + "View [" + node.getLeft() + "," + node.getTop()
                + " " + node.getWidth() + "x" + node.getHeight() + "]" + " " + node.getClassName());
        int id = node.getId();
        if (id != 0) {
            StringBuilder sb = new StringBuilder();
            sb.append(prefix); sb.append("  ID: #"); sb.append(Integer.toHexString(id));
            String entry = node.getIdEntry();
            if (entry != null) {
                String type = node.getIdType();
                String pkg = node.getIdPackage();
                sb.append(" "); sb.append(pkg); sb.append(":"); sb.append(type);
                sb.append("/"); sb.append(entry);
            }
            Log.i(TAG, sb.toString());
        }
        int scrollX = node.getScrollX();
        int scrollY = node.getScrollY();
        if (scrollX != 0 || scrollY != 0) {
            Log.i(TAG, prefix + "  Scroll: " + scrollX + "," + scrollY);
        }
        CharSequence contentDescription = node.getContentDescription();
        if (contentDescription != null) {
            Log.i(TAG, prefix + "  Content description: " + contentDescription);
        }
        CharSequence text = node.getText();
        if (text != null) {
            Log.i(TAG, prefix + "  Text (sel " + node.getTextSelectionStart() + "-"
                    + node.getTextSelectionEnd() + "): " + text);
            Log.i(TAG, prefix + "  Text size: " + node.getTextSize() + " , style: #"
                    + node.getTextStyle());
            Log.i(TAG, prefix + "  Text color fg: #" + Integer.toHexString(node.getTextColor())
                    + ", bg: #" + Integer.toHexString(node.getTextBackgroundColor()));
        }
        String hint = node.getHint();
        if (hint != null) {
            Log.i(TAG, prefix + "  Hint: " + hint);
        }
        Bundle extras = node.getExtras();
        if (extras != null) {
            Log.i(TAG, prefix + "  Extras: " + extras);
        }
        final int NCHILDREN = node.getChildCount();
        if (NCHILDREN > 0) {
            Log.i(TAG, prefix + "  Children:");
            String cprefix = prefix + "    ";
            for (int i=0; i<NCHILDREN; i++) {
                ViewNode cnode = node.getChildAt(i);
                dump(cprefix, cnode);
            }
        }
    }

    /**
     * Retrieve the framework-generated AssistStructure that is stored within
     * the Bundle filled in by {@link Activity#onProvideAssistData}.
     */
    public static AssistStructure getAssistStructure(Bundle assistBundle) {
        return assistBundle.getParcelable(ASSIST_KEY);
    }

    public ComponentName getActivityComponent() {
        return mActivityComponent;
    }

    /**
     * Return the number of window contents that have been collected in this assist data.
     */
    public int getWindowNodeCount() {
        return mWindowNodes.size();
    }

    /**
     * Return one of the windows in the assist data.
     * @param index Which window to retrieve, may be 0 to {@link #getWindowNodeCount()}-1.
     */
    public WindowNode getWindowNodeAt(int index) {
        return mWindowNodes.get(index);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        int start = out.dataPosition();
        PooledStringWriter pwriter = new PooledStringWriter(out);
        ComponentName.writeToParcel(mActivityComponent, out);
        final int N = mWindowNodes.size();
        out.writeInt(N);
        for (int i=0; i<N; i++) {
            mWindowNodes.get(i).writeToParcel(out, pwriter);
        }
        pwriter.finish();
        Log.i(TAG, "Flattened assist data: " + (out.dataPosition() - start) + " bytes");
    }

    public static final Parcelable.Creator<AssistStructure> CREATOR
            = new Parcelable.Creator<AssistStructure>() {
        public AssistStructure createFromParcel(Parcel in) {
            return new AssistStructure(in);
        }

        public AssistStructure[] newArray(int size) {
            return new AssistStructure[size];
        }
    };
}