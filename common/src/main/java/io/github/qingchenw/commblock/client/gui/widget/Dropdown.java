package io.github.qingchenw.commblock.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Dropdown<T> extends AbstractWidget {
    private static final int MAX_VISIBLE_OPTIONS = 5;
    private List<T> options;
    private ListEntryAdapter<T> adapter;
    private int selectedIndex;
    private boolean expanded;
    private int scrollOffset;

    public Dropdown(int x, int y, int width, int height, List<T> options, ListEntryAdapter<T> adapter, int selectedIndex, Component message) {
        super(x, y, width, height, message);
        this.options = options;
        this.setAdapter(adapter);
        this.selectedIndex = selectedIndex;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Font font = Minecraft.getInstance().font;
        int backgroundColor = this.isFocused() ? 0xFFFFFFFF : 0xFFA0A0A0;

        guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height,
                backgroundColor);
        guiGraphics.fill(this.getX() + 1, this.getY() + 1,
                this.getX() + this.width - 1, this.getY() + this.height - 1, 0xFF000000);

        adapter.render(guiGraphics, this.selectedIndex, this.getX(), this.getY(), false, partialTick);
        String arrow = this.expanded ? "▲" : "▼";
        guiGraphics.drawString(font, arrow, this.getX() + this.width - 15, this.getY() + (this.height - 8) / 2, 0xFFFFFF);

        if (this.expanded) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0.0D, 0.0D, 100.0D);

            int visibleOptions = Math.min(MAX_VISIBLE_OPTIONS, this.options.size());
            int dropHeight = visibleOptions * this.adapter.getHeight();

            guiGraphics.fill(this.getX(), this.getY() + this.height,
                    this.getX() + this.adapter.getWidth(), this.getY() + this.height + dropHeight, 0xFF000000);

            for (int i = 0; i < visibleOptions; i++) {
                int index = i + scrollOffset;
                if (index >= this.options.size()) break;

                int optionY = this.getY() + this.height + i * this.adapter.getHeight();
                boolean isHovered = mouseX >= this.getX() && mouseX <= this.getX() + this.adapter.getWidth()
                        && mouseY >= optionY && mouseY < optionY + this.adapter.getHeight();

                this.adapter.render(guiGraphics, index, this.getX(), optionY, isHovered, partialTick);
            }

            if (this.options.size() > MAX_VISIBLE_OPTIONS) {
                int scrollBarHeight = (visibleOptions * dropHeight) / this.options.size();
                int scrollBarY = this.getY() + this.height + (this.scrollOffset * dropHeight / this.options.size());

                guiGraphics.fill(this.getX() + this.width - 2, scrollBarY,
                        this.getX() + this.width, scrollBarY + scrollBarHeight, 0xFFCCCCCC);
            }

            guiGraphics.pose().popPose();
        }
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        int visibleOptions = this.expanded ? Math.min(this.options.size(), MAX_VISIBLE_OPTIONS) : 0;
        return this.active && this.visible && mouseX >= this.getX() && mouseX < this.getX() + this.width
                && mouseY >= this.getY() && mouseY < this.getY() + this.height + this.height * visibleOptions;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered()) {
            this.expanded = !this.expanded;
            return true;
        }

        if (this.expanded) {
            int visibleOptions = Math.min(this.options.size(), MAX_VISIBLE_OPTIONS);
            for (int i = 0; i < visibleOptions; i++) {
                int index = i + scrollOffset;
                if (index >= this.options.size()) break;

                int optionY = this.getY() + this.height + (i * this.height);
                if (mouseY >= optionY && mouseY < optionY + this.height
                        && mouseX >= this.getX() && mouseX < this.getX() + this.width) {
                    this.selectedIndex = index;
                    this.expanded = false;
                    return true;
                }
            }
        }

        if (this.expanded) {
            this.expanded = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (this.expanded && this.options.size() > MAX_VISIBLE_OPTIONS) {
            this.scrollOffset = Mth.clamp(this.scrollOffset - (int)delta,
                    0,
                    Math.max(0, this.options.size() - MAX_VISIBLE_OPTIONS));
            return true;
        }
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }

    public void setOptions(List<T> options) {
        this.options = options;
    }

    public void setAdapter(ListEntryAdapter<T> adapter) {
        this.adapter = adapter;
        this.adapter.parent = this;
    }

    public Optional<T> getOption(int index) {
        if (index < 0 || index >= this.options.size()) {
            return Optional.empty();
        }
        return Optional.ofNullable(this.options.get(index));
    }

    public int getSelectedIndex() {
        return this.selectedIndex;
    }

    public void setSelectedIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex;
    }

    public Optional<T> getSelectedOption() {
        return getOption(this.selectedIndex);
    }

    public void setSelection(T selection) {
        for (int i = 0; i < this.options.size(); i++) {
            if (this.adapter.areEntriesEqual(this.options.get(i), selection)) {
                this.selectedIndex = i;
                return;
            }
        }
        this.selectedIndex = 0;
    }

    public static abstract class ListEntryAdapter<T> {
        protected Dropdown<T> parent;

        public boolean areEntriesEqual(T entry1, T entry2) {
            return Objects.equals(entry1, entry2);
        }

        public int getWidth() {
            return this.parent.width;
        }

        public int getHeight() {
            return this.parent.height;
        }

        public abstract void render(GuiGraphics guiGraphics, int index, int x, int y, boolean isHovered, float partialTick);
    }
}
