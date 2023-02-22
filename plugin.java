package plugin;

import com.nomagic.actions.ActionsCategory;
import com.nomagic.actions.NMAction;
import com.nomagic.magicdraw.actions.ActionsConfiguratorsManager;
import com.nomagic.magicdraw.plugins.Plugin;
import myplugin7.MainMenu.MainMenuConfigurator;

import myplugin7.Function.AttributeModification;
import myplugin7.Function.XMLGenerator;

public class MyPlugin7 extends Plugin
{
    /**
     * Adding actions on plugin init.
     */
    @Override
    public void init()
    {
        ActionsConfiguratorsManager manager = ActionsConfiguratorsManager.getInstance();
        manager.addMainMenuConfigurator(new MainMenuConfigurator(getSeparatedActions()));  // adding actions with separator
    }

    @Override
    public boolean close()
    {
        return true;
    }

    @Override
    public boolean isSupported()
    {
        return true;
    }

    /**
     * Creates group of actions. This group is separated from others using menu separator (when it represented in menu).
     * Separator is added for group of actions in one actions category.
     */
    private static NMAction getSeparatedActions()
    {
        ActionsCategory category = new ActionsCategory();
        category.addAction(new XMLGenerator());
        category.addAction(new AttributeModification());
        return category;
    }
}
