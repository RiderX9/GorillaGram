package org.telegram.ui.Components;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.AndroidUtilities.dpf2;
import static org.telegram.messenger.AndroidUtilities.lerp;
import static org.telegram.messenger.LocaleController.getString;
import static org.telegram.ui.ActionBar.Theme.RIPPLE_MASK_CIRCLE_20DP;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.blur3.drawable.BlurredBackgroundDrawable;
import org.telegram.ui.Components.blur3.drawable.color.impl.BlurredBackgroundProviderImpl;
import org.telegram.ui.Stories.DarkThemeResourceProvider;
import org.telegram.ui.Stories.recorder.CaptionContainerView;
import org.telegram.ui.Stories.recorder.HintView2;

import xyz.nextalone.nagram.NaConfig;

public class CaptionPhotoViewer extends CaptionContainerView {

    private boolean addPhotoVisible;
    private final ImageView addPhotoButton;

    private boolean timerVisible;
    private final ImageView timerButton;
    private final PeriodDrawable timerDrawable;
    private ItemOptions timerPopup;
    private AiButtonDrawable aiButtonIcon;
    private ImageView aiButton;
    public HintView2 aiHint;

    private int timer = 0;
    private final int SHOW_ONCE = 0x7FFFFFFF;
    private final int[] values = new int[] { SHOW_ONCE, 3, 10, 30, 0 };

    private final HintView2 hint;
    private final Runnable applyCaption;

    private final RectF moveButtonBounds = new RectF();
    private final RectF doneButtonBounds = new RectF();
    private final ButtonBounce doneBounce = new ButtonBounce(this);
    private Paint donePaint;
    private Drawable doneDrawable;
    private Drawable moveButtonIcon;
    private final AnimatedTextView.AnimatedTextDrawable moveButtonText = new AnimatedTextView.AnimatedTextDrawable();
    private final ButtonBounce moveButtonBounce = new ButtonBounce(this);

    private BlurredBackgroundDrawable backgroundForCaptionButton;

    @Override
    protected int getEditTextStyle() {
        return EditTextEmoji.STYLE_PHOTOVIEWER;
    }

    public CaptionPhotoViewer(
        Context context,
        FrameLayout rootView,
        SizeNotifierFrameLayout sizeNotifierFrameLayout,
        FrameLayout containerView,
        Theme.ResourcesProvider resourcesProvider,
        BlurringShader.BlurManager blurManager,
        Runnable applyCaption
    ) {
        super(context, rootView, sizeNotifierFrameLayout, containerView, resourcesProvider, blurManager);
        this.applyCaption = applyCaption;

        moveButtonText.setTextSize(dp(14));
        moveButtonText.setOverrideFullWidth(AndroidUtilities.displaySize.x);
        moveButtonText.setTextColor(0xFFFFFFFF);
        if (isAtTop()) {
            moveButtonText.setText(getString(R.string.MoveCaptionDown));
            moveButtonIcon = context.getResources().getDrawable(R.drawable.menu_link_below);
        } else {
            moveButtonText.setText(getString(R.string.MoveCaptionUp));
            moveButtonIcon = context.getResources().getDrawable(R.drawable.menu_link_above);
        }

        addPhotoButton = new ImageView(context);
        addPhotoButton.setImageResource(R.drawable.filled_add_photo);
        addPhotoButton.setScaleType(ImageView.ScaleType.CENTER);
        addPhotoButton.setColorFilter(new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN));
        addPhotoButton.setBackground(Theme.createSelectorDrawable(Theme.ACTION_BAR_WHITE_SELECTOR_COLOR, RIPPLE_MASK_CIRCLE_20DP, dp(18)));
        setAddPhotoVisible(false, false);
        addView(addPhotoButton, LayoutHelper.createFrame(44, 44, Gravity.LEFT | (isAtTop() ? Gravity.TOP : Gravity.BOTTOM), 14, isAtTop() ? 6 : 0, 0, isAtTop() ? 0 : 6));

        timerButton = new ImageView(context);
        timerButton.setImageDrawable(timerDrawable = new PeriodDrawable());
        timerButton.setBackground(Theme.createSelectorDrawable(Theme.ACTION_BAR_WHITE_SELECTOR_COLOR, RIPPLE_MASK_CIRCLE_20DP, dp(18)));
        timerButton.setScaleType(ImageView.ScaleType.CENTER);
        setTimerVisible(false, false);
        addView(timerButton, LayoutHelper.createFrame(44, 44, Gravity.RIGHT | (isAtTop() ? Gravity.TOP : Gravity.BOTTOM), 0, isAtTop() ? 6 : 0, 10, isAtTop() ? 0 : 6));

        hint = new HintView2(context, isAtTop() ? HintView2.DIRECTION_TOP : HintView2.DIRECTION_BOTTOM);
        hint.setRounding(12);
        hint.setPadding(dp(12), dp(isAtTop() ? 8 : 0), dp(12), dp(isAtTop() ? 0 : 8));
        hint.setJoint(1, -21);
        hint.setMultilineText(true);
        addView(hint, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 80, Gravity.RIGHT | (isAtTop() ? Gravity.TOP : Gravity.BOTTOM)));

        aiButton = new ImageView(context);
        aiButton.setImageDrawable(aiButtonIcon = new AiButtonDrawable(context));
        aiButton.setScaleType(ImageView.ScaleType.CENTER);
        aiButton.setColorFilter(new PorterDuffColorFilter(0xFFFFFFFF, PorterDuff.Mode.SRC_IN));
        aiButton.setBackground(Theme.createSelectorDrawable(0x40FFFFFF, Theme.RIPPLE_MASK_CIRCLE_20DP, dp(16)));
        addView(aiButton, LayoutHelper.createFrame(44, 44, Gravity.TOP | Gravity.RIGHT, 8, 0, 8, 0));
        aiButton.setContentDescription(getString(R.string.AIEditor));
        ScaleStateListAnimator.apply(aiButton);

        if (tw.nekomimi.nekogram.NekoConfig.iOSMessageInputField.Bool()) {
            MarginLayoutParams mlp = (MarginLayoutParams) editText.getLayoutParams();
            mlp.leftMargin = dp(7);
            mlp.rightMargin = dp(7);
            editText.setLayoutParams(mlp);

            editText.getEditText().setLayoutParams(LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.LEFT | Gravity.CENTER_VERTICAL, 14, 0, 44, 0));
            editText.getEmojiButton().setLayoutParams(LayoutHelper.createFrame(40, 40, Gravity.BOTTOM | Gravity.RIGHT, 0, 0, 4, 0));
            editText.getEditText().setTranslationX(0);
        }

        editText.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable editable) {
                boolean hasText = editable != null && !TextUtils.isEmpty(editable.toString().trim());
                int lineCount = editText.getEditText().getLineCount();
                boolean show = hasText && (tw.nekomimi.nekogram.NekoConfig.iOSMessageInputField.Bool() ? (lineCount > 1) : (lineCount > 2));
                showAiButton(show);
            }
        });
        aiButton.setVisibility(View.GONE);
        aiButton.setAlpha(0.0f);
        aiButton.setScaleX(0.6f);
        aiButton.setScaleY(0.6f);
        aiButton.setOnClickListener(v -> openAiAlert());

        timerButton.setOnClickListener(e -> {
            if (timerPopup != null && timerPopup.isShown()) {
                timerPopup.dismiss();
                timerPopup = null;
                return;
            }
            hint.hide();

            timerPopup = ItemOptions.makeOptions(rootView, new DarkThemeResourceProvider(), timerButton);
            timerPopup.setDimAlpha(0);
            timerPopup.addText(getString(R.string.TimerPeriodHint), 13, dp(200));
            timerPopup.addGap();
            for (int value : values) {
                String text;
                if (value == 0) {
                    text = getString(R.string.TimerPeriodDoNotDelete);
                } else if (value == SHOW_ONCE) {
                    text = getString(R.string.TimerPeriodOnce);
                } else {
                    text = LocaleController.formatPluralString("Seconds", value);
                }
                timerPopup.add(0, text, () -> changeTimer(value));
                if (this.timer == value) {
                    timerPopup.putCheck();
                }
            }
            timerPopup.show();
        });
    }

    @Override
    protected void onLineCountChanged(int oldLineCount, int newLineCount) {
        final CharSequence text = getText();
        boolean hasText = text != null && !TextUtils.isEmpty(text.toString().trim());
        boolean show = hasText && (tw.nekomimi.nekogram.NekoConfig.iOSMessageInputField.Bool() ? (newLineCount > 1) : (newLineCount > 2));
        showAiButton(show);
        if (shownAiButton && (oldLineCount < 3) != (newLineCount < 3)) {
            invalidate();
        }
    }

    @Override
    public void clear() {
        super.clear();
        showAiButton(false);
    }

    @Override
    public void setText(CharSequence text) {
        super.setText(text);
    }

    private final AnimatedFloat lineCountAnimated = new AnimatedFloat(this, 0, 350, CubicBezierInterpolator.EASE_OUT_QUINT);

    private final AnimatedFloat moveButtonAnimated = new AnimatedFloat(this, 0, 350, CubicBezierInterpolator.EASE_OUT_QUINT);
    private final AnimatedFloat moveButtonExpandedAnimated = new AnimatedFloat(this, 0, 350, CubicBezierInterpolator.EASE_OUT_QUINT);
    private final AnimatedFloat aiPillExpandedAnimated = new AnimatedFloat(this, 0, 420, CubicBezierInterpolator.EASE_OUT_QUINT);
    private boolean moveButtonVisible;
    private boolean moveButtonExpanded;

    public void expandMoveButton() {
        AndroidUtilities.cancelRunOnUIThread(collapseMoveButton);
        moveButtonExpanded = MessagesController.getInstance(currentAccount).shouldShowMoveCaptionHint();
        if (moveButtonExpanded) {
            MessagesController.getInstance(currentAccount).incrementMoveCaptionHint();
            invalidate();
            AndroidUtilities.runOnUIThread(collapseMoveButton, 5000);
        }
    }

    private final Runnable collapseMoveButton = () -> {
        if (moveButtonExpanded) {
            moveButtonExpanded = false;
            invalidate();
        }
    };

    protected void openedKeyboard() {
        expandMoveButton();
    }

    @Override
    public void updateKeyboard(int keyboardHeight) {
        final boolean wasOpen = super.toKeyboardShow;
        super.updateKeyboard(keyboardHeight);
        if (!wasOpen && keyboardNotifier.keyboardVisible()) {
            openedKeyboard();
        }
    }

    @Override
    public int getPadHLeft() {
        if (tw.nekomimi.nekogram.NekoConfig.iOSMessageInputField.Bool()) {
            return (int) AndroidUtilities.lerp(dp(7), dp(58), keyboardT);
        }
        return super.getPadHLeft();
    }

    @Override
    public int getPadHRight() {
        if (tw.nekomimi.nekogram.NekoConfig.iOSMessageInputField.Bool()) {
            return (int) AndroidUtilities.lerp(dp(7), dp(58), keyboardT);
        }
        return super.getPadHRight();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        final boolean ios = tw.nekomimi.nekogram.NekoConfig.iOSMessageInputField.Bool();
        final float aiFactor = ios ? aiPillExpandedAnimated.set(shownAiButton ? 1.0f : 0.0f) : 0f;

        final float moveButtonAlpha = ios ? keyboardT : moveButtonAnimated.set(moveButtonVisible, !showMoveButton());
        final float moveButtonExpanded = moveButtonExpandedAnimated.set(this.moveButtonExpanded);
        if (moveButtonAlpha > 0.0f) {
            float s = moveButtonBounce.getScale(.03f);
            final int offset = dp(4 * (1f - keyboardT));
            if (ios) {
                float left = dp(7);
                float right = left + dp(44);
                if (isAtTop()) {
                    moveButtonBounds.set(left, bounds.top, right, bounds.top + dp(44) + dp(44) * aiFactor);
                } else {
                    moveButtonBounds.set(left, bounds.bottom - dp(44) - dp(44) * aiFactor, right, bounds.bottom);
                }
            } else {
                if (isAtTop()) {
                    moveButtonBounds.set(offset + dp(7), bounds.bottom + dp(10), offset + dp(7 + 37) + (moveButtonText.getCurrentWidth() + dp(11)) * moveButtonExpanded, bounds.bottom + dp(10 + 32) + dp(40) * aiFactor);
                } else {
                    moveButtonBounds.set(offset + dp(7), bounds.top - dp(32 + 10) - dp(40) * aiFactor, offset + dp(7 + 37) + (moveButtonText.getCurrentWidth() + dp(11)) * moveButtonExpanded, bounds.top - dp(10));
                }
            }
            if (moveButtonAlpha < 1) {
                canvas.saveLayerAlpha(moveButtonBounds, (int) (0xFF * moveButtonAlpha), Canvas.ALL_SAVE_FLAG);
            } else {
                canvas.save();
            }
            canvas.scale(s, s, moveButtonBounds.centerX(), moveButtonBounds.centerY());
            canvas.clipRect(moveButtonBounds);

            if (factoryForMentions != null) {
                if (backgroundForCaptionButton == null) {
                    backgroundForCaptionButton = factoryForMentions.create(this)
                        .setColorProvider(BlurredBackgroundProviderImpl.photoViewer(resourcesProvider))
                        .setPadding(dp(5))
                        .setRadius(ios ? dp(22) : dp(16));
                }

                moveButtonBounds.round(AndroidUtilities.rectTmp2);
                AndroidUtilities.rectTmp2.inset(-dp(5), -dp(5));
                backgroundForCaptionButton.setBounds(AndroidUtilities.rectTmp2);
                backgroundForCaptionButton.draw(canvas);
            }
            int iconCenterY = ios ? (int) (isAtTop() ? (moveButtonBounds.top + dp(22)) : (moveButtonBounds.bottom - dp(22))) : (int) (isAtTop() ? (moveButtonBounds.top + dp(16)) : (moveButtonBounds.bottom - dp(16)));
            int iconCenterX = ios ? (int) moveButtonBounds.centerX() : (int) (moveButtonBounds.left + dp(19));
            moveButtonIcon.setBounds(iconCenterX - dp(10), iconCenterY - dp(10), iconCenterX + dp(10), iconCenterY + dp(10));
            moveButtonIcon.draw(canvas);
            if (!ios) {
                moveButtonText.setBounds(moveButtonBounds.left + dp(37), moveButtonBounds.top, moveButtonBounds.right, moveButtonBounds.bottom);
                moveButtonText.setAlpha((int) (0xFF * moveButtonExpanded));
                moveButtonText.draw(canvas);
            } else if (aiFactor > 0) {
                int aiCenterY = (int) (isAtTop() ? (moveButtonBounds.bottom - dp(22)) : (moveButtonBounds.top + dp(22)));
                aiButtonIcon.setBounds(iconCenterX - dp(12), aiCenterY - dp(12), iconCenterX + dp(12), aiCenterY + dp(12));
                aiButtonIcon.setAlpha((int) (0xFF * aiFactor));
                aiButtonIcon.draw(canvas);
            }
            canvas.restore();
        }

        if (aiButton != null && !ios) {
            aiButton.setTranslationX(-dp(4 * (1f - keyboardT)));
            aiButton.setTranslationY(
                (isAtTop() ? bounds.bottom - dp(44) : bounds.top) +
                (isAtTop() ? 1 : -1) * dp(3) * Utilities.clamp01(-lineCountAnimated.set(editText.getEditText().getLineCount()) + 4)
            );
        }

        if (ios && keyboardT > 0) {
            float right = getWidth() - dp(7);
            float left = right - dp(44);
            float top = isAtTop() ? bounds.top : (bounds.bottom - dp(44));
            float bottom = top + dp(44);
            doneButtonBounds.set(left, top, right, bottom);

            float s = doneBounce.getScale(0.04f) * Math.min(1.0f, keyboardT * 1.5f);
            canvas.save();
            canvas.scale(s, s, doneButtonBounds.centerX(), doneButtonBounds.centerY());
            if (keyboardT < 1f) {
                canvas.saveLayerAlpha(doneButtonBounds, (int) (0xFF * keyboardT), Canvas.ALL_SAVE_FLAG);
            }

            if (donePaint == null) {
                donePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                donePaint.setColor(0xFFFFFFFF);
            }
            canvas.drawCircle(doneButtonBounds.centerX(), doneButtonBounds.centerY(), dp(22), donePaint);

            if (doneDrawable == null) {
                doneDrawable = getContext().getResources().getDrawable(R.drawable.input_done).mutate();
                doneDrawable.setColorFilter(new PorterDuffColorFilter(0xFF000000, PorterDuff.Mode.SRC_IN));
            }
            int cx = (int) doneButtonBounds.centerX();
            int cy = (int) doneButtonBounds.centerY();
            doneDrawable.setBounds(cx - dp(12), cy - dp(12), cx + dp(12), cy + dp(12));
            doneDrawable.draw(canvas);

            if (keyboardT < 1f) {
                canvas.restore();
            }
            canvas.restore();
        }

        super.dispatchDraw(canvas);
    }

    public void setOnAddPhotoClick(View.OnClickListener listener) {
        addPhotoButton.setOnClickListener(listener);
    }

    public void setAddPhotoVisible(boolean visible, boolean animated) {
        addPhotoVisible = visible;
        addPhotoButton.animate().cancel();
        if (animated) {
            addPhotoButton.setVisibility(View.VISIBLE);
            addPhotoButton.animate().alpha(visible ? 1f : 0f).translationX(visible ? 0 : dp(-8)).withEndAction(() -> {
                if (!visible) {
                    timerButton.setVisibility(View.GONE);
                }
            }).start();
        } else {
            addPhotoButton.setVisibility(visible ? View.VISIBLE : View.GONE);
            addPhotoButton.setAlpha(visible ? 1f : 0f);
            addPhotoButton.setTranslationX(visible ? 0 : dp(-8));
        }
        updateEditTextLeft();

        if (!tw.nekomimi.nekogram.NekoConfig.iOSMessageInputField.Bool()) {
            MarginLayoutParams lp = (MarginLayoutParams) editText.getLayoutParams();
            lp.rightMargin = dp(32 + (addPhotoVisible && timerVisible ? 33 : 0));
            editText.setLayoutParams(lp);
        }
    }

    @Override
    protected int getEditTextLeft() {
        if (tw.nekomimi.nekogram.NekoConfig.iOSMessageInputField.Bool()) {
            return dp(26);
        }
        return addPhotoVisible ? dp(31) : 0;
    }

    private boolean isVideo;
    public void setIsVideo(boolean isVideo) {
        this.isVideo = isVideo;
    }

    @Override
    protected void onTextChange() {
        if (applyCaption != null) {
            applyCaption.run();
        }
    }

    public void setTimerVisible(boolean visible, boolean animated) {
        timerVisible = visible;
        timerButton.animate().cancel();
        if (animated) {
            timerButton.setVisibility(View.VISIBLE);
            timerButton.animate().alpha(visible ? 1f : 0f).translationX(visible ? 0 : dp(8)).withEndAction(() -> {
                if (!visible) {
                    timerButton.setVisibility(View.GONE);
                }
            }).start();
        } else {
            timerButton.setVisibility(visible ? View.VISIBLE : View.GONE);
            timerButton.setAlpha(visible ? 1f : 0f);
            timerButton.setTranslationX(visible ? 0 : dp(8));
        }

        if (!tw.nekomimi.nekogram.NekoConfig.iOSMessageInputField.Bool()) {
            MarginLayoutParams lp = (MarginLayoutParams) editText.getLayoutParams();
            lp.rightMargin = dp(32 + (addPhotoVisible && timerVisible ? 33 : 0));
            editText.setLayoutParams(lp);
        }
    }

    public boolean hasTimer() {
        return timerVisible && timer > 0;
    }

    public void setTimer(int value) {
        this.timer = value;
        timerDrawable.setValue(timer == SHOW_ONCE ? 1 : Math.max(1, timer), timer > 0, true);
        if (hint != null) {
            hint.hide();
        }
    }

    private void changeTimer(int value) {
        if (this.timer == value) {
            return;
        }
        setTimer(value);
        if (onTTLChange != null) {
            onTTLChange.run(value);
        }
        CharSequence text;
        if (value == 0) {
            text = getString(isVideo ? R.string.TimerPeriodVideoKeep : R.string.TimerPeriodPhotoKeep);
            hint.setMaxWidthPx(getMeasuredWidth());
            hint.setMultilineText(false);
            hint.setInnerPadding(13, 4, 10, 4);
            hint.setIconMargin(0);
            hint.setIconTranslate(0, -dp(1));
        } else if (value == SHOW_ONCE) {
            text = getString(isVideo ? R.string.TimerPeriodVideoSetOnce : R.string.TimerPeriodPhotoSetOnce);
            hint.setMaxWidthPx(getMeasuredWidth());
            hint.setMultilineText(false);
            hint.setInnerPadding(13, 4, 10, 4);
            hint.setIconMargin(0);
            hint.setIconTranslate(0, -dp(1));
        } else if (value > 0) {
            text = AndroidUtilities.replaceTags(LocaleController.formatPluralString(isVideo ? "TimerPeriodVideoSetSeconds" : "TimerPeriodPhotoSetSeconds", value));
            hint.setMultilineText(true);
            hint.setMaxWidthPx(HintView2.cutInFancyHalf(text, hint.getTextPaint()));
            hint.setInnerPadding(12, 7, 11, 7);
            hint.setIconMargin(2);
            hint.setIconTranslate(0, 0);
        } else {
            return;
        }
        hint.setTranslationY((-Math.min(dp(34), getEditTextHeight()) - dp(14)) * (isAtTop() ? -1.0f : 1.0f));
        hint.setText(text);
        final int iconResId = value > 0 ? R.raw.fire_on : R.raw.fire_off;
        RLottieDrawable icon = new RLottieDrawable(iconResId, "" + iconResId, dp(34), dp(34));
        icon.start();
        hint.setIcon(icon);
        hint.show();

        moveButtonExpanded = false;
        AndroidUtilities.cancelRunOnUIThread(collapseMoveButton);
        invalidate();
    }

    @Override
    protected void onEditHeightChange(int height) {
        hint.setTranslationY((-Math.min(dp(34), height) - dp(10)) * (isAtTop() ? -1.0f : 1.0f));
    }

    @Override
    protected boolean clipChild(View child) {
        return child != hint;
    }

    private Utilities.Callback<Integer> onTTLChange;
    public void setOnTimerChange(Utilities.Callback<Integer> onTTLChange) {
        this.onTTLChange = onTTLChange;
    }

    @Override
    protected int getCaptionLimit() {
        return UserConfig.getInstance(currentAccount).isPremium() ? getCaptionPremiumLimit() : getCaptionDefaultLimit();
    }

    @Override
    protected int getCaptionDefaultLimit() {
        return MessagesController.getInstance(currentAccount).captionLengthLimitDefault;
    }

    @Override
    protected int getCaptionPremiumLimit() {
        return MessagesController.getInstance(currentAccount).captionLengthLimitPremium;
    }

    @Override
    protected void beforeUpdateShownKeyboard(boolean show) {
        if (!show) {
            timerButton.setVisibility(timerVisible ? View.VISIBLE : View.GONE);
            addPhotoButton.setVisibility(addPhotoVisible ? View.VISIBLE : View.GONE);
        }
        if (hint != null) {
            hint.hide();
        }
    }

    @Override
    protected void onUpdateShowKeyboard(float keyboardT) {
        timerButton.setAlpha(1f - keyboardT);
        addPhotoButton.setAlpha(1f - keyboardT);
        if (tw.nekomimi.nekogram.NekoConfig.iOSMessageInputField.Bool()) {
            int padLeft = (int) AndroidUtilities.lerp(dp(7), dp(58), keyboardT);
            int padRight = (int) AndroidUtilities.lerp(dp(7), dp(58), keyboardT);
            MarginLayoutParams mlp = (MarginLayoutParams) editText.getLayoutParams();
            if (mlp.leftMargin != padLeft || mlp.rightMargin != padRight) {
                mlp.leftMargin = padLeft;
                mlp.rightMargin = padRight;
                editText.setLayoutParams(mlp);
            }
            editText.getEditText().setTranslationX(0);
        }
    }

    @Override
    public void updateEditTextLeft() {
        if (tw.nekomimi.nekogram.NekoConfig.iOSMessageInputField.Bool()) {
            editText.getEditText().setTranslationX(0);
            return;
        }
        super.updateEditTextLeft();
    }

    @Override
    protected void afterUpdateShownKeyboard(boolean show) {
        timerButton.setVisibility(!show && timerVisible ? View.VISIBLE : View.GONE);
        addPhotoButton.setVisibility(!show && addPhotoVisible ? View.VISIBLE : View.GONE);
        if (show) {
            timerButton.setVisibility(View.GONE);
            addPhotoButton.setVisibility(View.GONE);
        }
    }

    @Override
    protected int additionalKeyboardHeight() {
        return 0;
    }

    @Override
    public void updateColors(Theme.ResourcesProvider resourcesProvider) {
        super.updateColors(resourcesProvider);
        timerDrawable.updateColors(0xffffffff, Theme.getColor(Theme.key_chat_editMediaButton, resourcesProvider), 0xffffffff);
    }

    @Override
    protected void setupMentionContainer() {

    }

    protected boolean showMoveButton() {
        return false;
    }

    public void setShowMoveButtonVisible(boolean visible, boolean animated) {
        if (moveButtonVisible == visible && animated) return;
        moveButtonVisible = visible;
        if (!animated) {
            moveButtonAnimated.set(visible, true);
        }
        invalidate();
    }

    protected void onMoveButtonClick() {

    }

    @Override
    public int getEditTextHeight() {
        return super.getEditTextHeight();
    }

    private boolean aiPressed;

    private void openAiAlert() {
        MessagesController.getGlobalMainSettings().edit().putInt("aihintshown", 3).apply();
        new AIEditorAlert(getContext(), new DarkThemeResourceProvider())
            .setText(editText.getText())
            .setOnUse(text -> {
                editText.setText(text);
                editText.setSelection(text.length(), text.length());
            })
            .setOnSend(0, true, (text, scheduleDate, scheduleRepeatPeriod, notify) -> {
                editText.setText(text);
                done();
            })
            .show();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        final boolean ios = tw.nekomimi.nekogram.NekoConfig.iOSMessageInputField.Bool();
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (ios && keyboardT > 0 && doneButtonBounds.contains(event.getX(), event.getY())) {
                doneBounce.setPressed(true);
                return true;
            }
            if (ios && shownAiButton && aiPillExpandedAnimated.get() > 0.3f) {
                float aiTop = isAtTop() ? (moveButtonBounds.bottom - dp(44)) : moveButtonBounds.top;
                float aiBottom = isAtTop() ? moveButtonBounds.bottom : (moveButtonBounds.top + dp(44));
                if (event.getX() >= moveButtonBounds.left && event.getX() <= moveButtonBounds.right && event.getY() >= aiTop && event.getY() <= aiBottom) {
                    aiPressed = true;
                    return true;
                }
            }
            boolean canPressMove = ios ? (keyboardT > 0 || moveButtonVisible) : (moveButtonAnimated.get() > 0);
            moveButtonBounce.setPressed(canPressMove && moveButtonBounds.contains(event.getX(), event.getY()));
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (doneBounce.isPressed() && !doneButtonBounds.contains(event.getX(), event.getY())) {
                doneBounce.setPressed(false);
            }
            if (aiPressed) {
                float aiTop = isAtTop() ? (moveButtonBounds.bottom - dp(44)) : moveButtonBounds.top;
                float aiBottom = isAtTop() ? moveButtonBounds.bottom : (moveButtonBounds.top + dp(44));
                if (event.getX() < moveButtonBounds.left || event.getX() > moveButtonBounds.right || event.getY() < aiTop || event.getY() > aiBottom) {
                    aiPressed = false;
                }
            }
            boolean canPressMove = ios ? (keyboardT > 0 || moveButtonVisible) : (moveButtonAnimated.get() > 0);
            if (moveButtonBounce.isPressed() && (!canPressMove || !moveButtonBounds.contains(event.getX(), event.getY()))) {
                moveButtonBounce.setPressed(false);
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
            if (doneBounce.isPressed()) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    done();
                }
                doneBounce.setPressed(false);
                return true;
            }
            if (aiPressed) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    openAiAlert();
                }
                aiPressed = false;
                return true;
            }
            if (moveButtonBounce.isPressed()) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    onMoveButtonClick();
                    moveButtonText.setText(getString(isAtTop() ? R.string.MoveCaptionDown : R.string.MoveCaptionUp), true);
                }
                moveButtonBounce.setPressed(false);
                return true;
            }
        }
        return doneBounce.isPressed() || aiPressed || moveButtonBounce.isPressed() || super.dispatchTouchEvent(event);
    }


    private boolean shownAiButton;
    private void showAiButton(boolean show_) {
        final boolean show = show_ && !NaConfig.INSTANCE.getHideAiEditor().Bool();

        if (shownAiButton == show) return;
        if (show) {
            MessagesController.getInstance(currentAccount).getTonesController().load();
        }
        shownAiButton = show;
        invalidate();
        if (tw.nekomimi.nekogram.NekoConfig.iOSMessageInputField.Bool()) {
            if (show) {
                postDelayed(aiButtonIcon::animate, 220);
            }
            return;
        }
        aiButton.setVisibility(View.VISIBLE);
        aiButton.animate()
            .alpha(show ? 1.0f : 0.0f)
            .scaleX(show ? 1.0f : 0.6f)
            .scaleY(show ? 1.0f : 0.6f)
            .setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT)
            .setDuration(420)
            .withEndAction(() -> {
                if (!show) {
                    aiButton.setVisibility(View.GONE);
                }
            })
            .start();
        if (show) {
            aiButton.postDelayed(aiButtonIcon::animate, 220);

            if (aiHint != null) {
                aiHint.hide();
                aiHint = null;
            }

            if (
                MessagesController.getGlobalMainSettings().getInt("aihintshown", 0) < 3
            ) {
                final HintView2 thisHint = aiHint = new HintView2(getContext(), HintView2.DIRECTION_BOTTOM);
                aiHint.setMultilineText(true);
                aiHint.setText(getString(R.string.AIEditorHint));
                aiHint.setJointPx(1f, -aiButton.getWidth() / 2f + dp(4));
                addView(aiHint, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 200, Gravity.TOP, 0, -200 + 4, 0, 0));
                aiHint.setOnHiddenListener(() -> removeView(thisHint));
                aiHint.setDuration(4000L);
                aiHint.show();
                MessagesController.getGlobalMainSettings().edit().putInt("aihintshown",
                    MessagesController.getGlobalMainSettings().getInt("aihintshown", 0) + 1
                ).apply();
            }
        } else {
            if (aiHint != null) {
                aiHint.hide();
                aiHint = null;
            }
        }
    }
}
