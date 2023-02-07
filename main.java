package myplugin3;

import com.nomagic.actions.AMConfigurator;
import com.nomagic.actions.ActionsManager;
import com.nomagic.magicdraw.actions.ActionsConfiguratorsManager;
import com.nomagic.magicdraw.actions.BrowserContextAMConfigurator;
import com.nomagic.magicdraw.actions.MDActionsCategory;
import com.nomagic.magicdraw.plugins.Plugin;
import com.nomagic.magicdraw.ui.browser.Node;
import com.nomagic.magicdraw.ui.browser.Tree;
import com.nomagic.magicdraw.ui.browser.actions.DefaultBrowserAction;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.magicdraw.uml.ClassTypes;
import com.nomagic.uml2.ext.jmi.reflect.VisitorContext;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Comment;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ElementTaggedValue;
import com.nomagic.uml2.impl.ModelHierarchyVisitor;
import com.nomagic.uml2.ext.magicdraw.activities.mdintermediateactivities.ActivityPartition;

import javax.annotation.CheckForNull;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.*;

/**
 * Example-Plugin for counting model elements and doing some kind of analysis or statistics.
 * Demonstrates how to collect children (contained elements) of some element,
 * how to go through hierarchy and do some element-type specific actions by using InheritanceVisitor.
 */
@SuppressWarnings({"squid:S106", "squid:S1148", "UnusedDeclaration"})
public class MyPlugin3 extends Plugin
{
    /**
     * Action, that do all the work.
     */
    private ShowStatistics statsAction;

    /**
     * Hierarchy visitor, for counting elements.
     * Can do different actions with different element types.
     */
    private final StatisticsVisitor visitor = new StatisticsVisitor();

    /**
     * Initializing the plugin.
     * Create and register action.
     */
    @Override
    public void init()
    {
        // create browser action.
        statsAction = new ShowStatistics();

        // register this action in containment tree browser.
        ActionsConfiguratorsManager.getInstance()
                .addContainmentBrowserContextConfigurator(new BrowserContextAMConfigurator()
                {
                    @Override
                    public void configure(ActionsManager manager, Tree browser)
                    {
                        if (statsAction.canBeUsed(browser))
                        {
                            MDActionsCategory category = new MDActionsCategory();
                            category.addAction(statsAction);
                            manager.addCategory(category);
                        }
                    }

                    @Override
                    public int getPriority()
                    {
                        return AMConfigurator.MEDIUM_PRIORITY;
                    }
                });
    }

    /**
     * Return true always, because this plugin does not have any close specific actions.
     */
    @Override
    public boolean close()
    {
        return true;
    }

    /**
     * @see com.nomagic.magicdraw.plugins.Plugin#isSupported()
     */
    @Override
    public boolean isSupported()
    {
        return true;
    }

    /**
     * The action responsible for starting new counter and displaying results.
     * Can be invoked on every model element node in the browser tree.
     */
    class ShowStatistics extends DefaultBrowserAction
    {
        public ShowStatistics()
        {
            super("", "Show Statistics", null, null);
        }

        /**
         * Performs new count.
         * Clear results map, starts new counter and displays results.
         */
        @Override
        public void actionPerformed(ActionEvent evt)
        {
            visitor.getMap().clear();
            // count children of selected in browser element.
            Tree tree = getTree();
            if (tree != null)
            {
                final Element element = getSelectedElement(tree);
                if (element != null)
                {
                    visitChildren(element);
                }
                showResults(visitor.getMap());
            }
        }

        /**
         * Displays counting results - data from counter's elements map.
         * Simply shows Swing message with counting results.
         */
        public void showResults(Map<String, Integer> map)
        {
            // this text will be displayed in message window.
            StringBuilder text = new StringBuilder();

            // constructs text in style :
            // Type1 : elements number
            // Type2 : elements number
            // ...
            for (String type : map.keySet())
            {
                text.append(type).append(" : ").append(map.get(type)).append("\n");
            }

            // do some analysis - average number of attributes and operation per class.
            Integer classes = map.get("ModelClass");
            Integer attributes = map.get("Attribute");
            Integer operations = map.get("Operation");

            if (classes != null && operations != null)
            {
                text.append("Average operations per class : ").append(operations.doubleValue() / classes.doubleValue()).append("\n");
            }

            if (classes != null && attributes != null)
            {
                text.append("Average attributes per class : ").append(attributes.doubleValue() / classes.doubleValue()).append("\n");
            }

            // if map was empty and no elements was found.
            if (text.length() == 0)
            {
                text = new StringBuilder("No elements found!");

            }

            // displays constructed text.
            JOptionPane.showMessageDialog(null, text.toString(), "Statistics", JOptionPane.PLAIN_MESSAGE);
        }

        /**
         * Action can be used only if there are selected model element in the browser.
         */
        public boolean canBeUsed(Tree tree)
        {
            return getSelectedElement(tree) != null;
        }

        /**
         * Update menu item.
         */
        public void updateState(Tree tree)
        {
            setEnabled(canBeUsed(tree));
        }

        /**
         * @return selected in given tree model element or null otherwise.
         */
        @CheckForNull
        public Element getSelectedElement(Tree tree)
        {
            if(tree.getSelectedNodes() == null)
            {
                return null;
            }
            // iterate selected nodes.
            // checks type of the node, because can be selected and code engineering sets.
            return (Element) Arrays.stream(tree.getSelectedNodes())
                    .map(Node::getUserObject)
                    .filter(userObject -> userObject instanceof Element)
                    .findFirst()
                    .orElse(null);
            // if there is no selected model element.
        }

        /**
         * Goes through all children of given model elements.
         * Demonstrates way how to collect all children by using FOR cycle and avoiding recursion.
         * Visit every child with StatisticsVisitor.
         *
         * @param root the root model element.
         */
        public void visitChildren(Element root)
        {
            ArrayList<Element> all = new ArrayList<>();
            all.add(root);

            // if current element has children, list will be increased.
            for (int i = 0; i < all.size(); i++)
            {
                Element current = all.get(i);
                try
                {
                    // let's perform some action with this element in visitor.
                    current.accept(visitor);

                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                // add all children into end of this list, so it emulates recursion.
                all.addAll(current.getOwnedElement());
            }
        }
    }

    /**
     * Visitor for counting model elements.
     * Contains Map for counting results.
     * Must be overridden visit... method for every counted type of element.
     * In this example only ModelClass, Attribute and Operation will be counted.
     */
    static class StatisticsVisitor extends ModelHierarchyVisitor
    {
        /**
         * Map for counting results.
         * Class type is a key, number is a value.
         */
        private final HashMap<String, Integer> statsTable = new HashMap<>();

        /**
         * Returns map of results. Used to access results from outside.
         *
         * @return results map.
         */
        public Map<String, Integer> getMap()
        {
            return statsTable;
        }

        @Override
        public void visitElement(com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element element,
                                 VisitorContext context)
        {
            super.visitElement(element, context);
            count(element);
            printInfParent(element);
            System.out.println();
        }

        /**
         * Increases value of this type count in results map.
         * If there is no such registered type, add it.
         * @param element the element to count.
         */
        public void count(Element element)
        {
            String classType = ClassTypes.getShortName(element.getClassType());
            Integer count = statsTable.get(classType);
            if (count==null)
            {
                count = 0;
            }

            statsTable.put(classType, count + 1);
        }

        // Print element information
        public void printInf(Element element)
        {
            String classType = ClassTypes.getShortName(element.getClassType());  // The type of element
            String classID = element.getID();  // The ID of element
            String humanName = element.getHumanName();  // The human name of element
            String humanType = element.getHumanType();  // The human type of element

            // Print in the console
            System.out.println("Element ID: " + classID);
            System.out.println("Class Type: " + classType);
            System.out.println("Human Name: " + humanName);
            System.out.println("Human Type: " + humanType);
        }

        // Print the owner element information
        public void printInfOwner(Element element)
        {
            Element owner = element.getOwner();
            String ownerID = null;
            String ownerClassType = null;
            if (owner != null)
            {
                ownerID = owner.getID();
                ownerClassType = ClassTypes.getShortName(owner.getClassType());
            }
            System.out.println("Owner ID: " + ownerID);
            System.out.println("Owner Class Type: " + ownerClassType);
        }

        // Print the sync element information
        public void printInfSync(Element element)
        {
            Element sync = element.getSyncElement();
            String syncID = null;
            String syncClassType = null;
            if (sync != null)
            {
                syncID = sync.getID();
            }
            if (sync != null)
            {
                syncClassType = ClassTypes.getShortName(sync.getClassType());
            }
            System.out.println("Sync ID: " + syncID);
            System.out.println("Sync Class Type: " + syncClassType);
        }

        // Print the element parent information
        public void printInfParent(Element element)
        {
            BaseElement parent = element.getObjectParent();
            String parentID = parent.getID();
            String parentClassType = ClassTypes.getShortName(parent.getClassType());
            String select = parentClassType + "[@id=" + "'" + parentID + "'" + "]";
            System.out.println("Parent ID: " + parentID);
            System.out.println("Parent Class Type: " + parentClassType);
            System.out.println(select);
        }
    }
}
