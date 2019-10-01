package io.github.qingchenw.communicationblock.client;

import java.io.IOException;
import java.util.List;

import org.lwjgl.input.Keyboard;

import io.github.qingchenw.communicationblock.MessageUpdateCommBlock;
import io.github.qingchenw.communicationblock.SerialPortMod.CommonProxy;
import io.github.qingchenw.communicationblock.utils.SerialPortManager;
import io.github.qingchenw.communicationblock.TileEntityCommunicationBlock;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiCommunicationBlock extends GuiScreen
{
	private List<String> ports;
	private int index;
	
    private final TileEntityCommunicationBlock communicationBlock;
    private GuiTextField dataField;
    private GuiButton portButton;
    private GuiButton doneButton;
    private GuiButton cancelButton;

    public GuiCommunicationBlock(TileEntityCommunicationBlock te)
    {
        this.communicationBlock = te;
    }

    @Override
	public void initGui()
    {
    	ports = SerialPortManager.findPorts();
    	index = ports.indexOf(communicationBlock.getPort());
    	if (index < 0) index = 0;
    	
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();
        this.doneButton = this.addButton(new GuiButton(0, this.width / 2 - 4 - 150, this.height / 4 + 120 + 12, 150, 20, I18n.format("gui.done")));
        this.cancelButton = this.addButton(new GuiButton(1, this.width / 2 + 4, this.height / 4 + 120 + 12, 150, 20, I18n.format("gui.cancel")));
        this.dataField = new GuiTextField(2, this.fontRenderer, this.width / 2 - 150, 50, 300, 20);
        this.dataField.setMaxStringLength(32500);
        this.dataField.setFocused(true);
        this.dataField.setText(this.communicationBlock.getData());
        this.portButton = this.addButton(new GuiButton(3, this.width / 2 - 150, 72, 100, 20, !ports.isEmpty() ? ports.get(index) : ""));
        this.doneButton.enabled = !this.dataField.getText().trim().isEmpty();
    }
    
    @Override
	public void updateScreen()
    {
        this.dataField.updateCursorCounter();
    }
    
    @Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRenderer, I18n.format("commMod.setCommand"), this.width / 2, 20, 16777215);
        this.drawString(this.fontRenderer, I18n.format("commMod.command"), this.width / 2 - 150, 40, 10526880);
        this.dataField.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    
    public void updateGui()
    {
    	ports = SerialPortManager.findPorts();
    	index = ports.indexOf(communicationBlock.getPort());
    	if (index < 0) index = 0;
    	
    	this.dataField.setText(communicationBlock.getData());
    	this.portButton.displayString = ports.get(index);
    	this.doneButton.enabled = true;
    }

    @Override
	public void onGuiClosed()
    {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.dataField.mouseClicked(mouseX, mouseY, mouseButton);
    }
    
    @Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        this.dataField.textboxKeyTyped(typedChar, keyCode);
        this.doneButton.enabled = !this.dataField.getText().trim().isEmpty();

        if (keyCode != 28 && keyCode != 156)
        {
            if (keyCode == 1)
            {
                this.actionPerformed(this.cancelButton);
            }
        }
        else
        {
            this.actionPerformed(this.doneButton);
        }
    }

    @Override
	protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.enabled)
        {
            if (button.id == 1)
            {
                this.mc.displayGuiScreen(null);
            }
            else if (button.id == 0)
            {
            	CommonProxy.instance.sendToServer(new MessageUpdateCommBlock(
            			this.communicationBlock.getPos().getX(),
                		this.communicationBlock.getPos().getY(),
                		this.communicationBlock.getPos().getZ(),
                		this.portButton.displayString,
                		this.dataField.getText()));
                this.mc.displayGuiScreen(null);
            }
            else if (button.id == 3)
            {
            	++index;
            	if (index >= ports.size()) index = 0;
            	this.portButton.displayString = ports.get(index);
            }
        }
    }
}
