package myplugin5;

import com.nomagic.actions.AMConfigurator;
import com.nomagic.actions.ActionsManager;
import com.nomagic.magicdraw.actions.ActionsConfiguratorsManager;
import com.nomagic.magicdraw.actions.BrowserContextAMConfigurator;
import com.nomagic.magicdraw.actions.MDActionsCategory;
import com.nomagic.magicdraw.plugins.Plugin;
import com.nomagic.magicdraw.ui.browser.Node;
import com.nomagic.magicdraw.ui.browser.Tree;
import com.nomagic.magicdraw.ui.browser.actions.DefaultBrowserAction;
import com.nomagic.magicdraw.uml.ClassTypes;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralUnlimitedNatural;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdcommunications.Trigger;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdsimpletime.TimeExpression;
import com.nomagic.uml2.impl.ModelHierarchyVisitor;

import com.nomagic.uml2.ext.magicdraw.activities.mdbasicactivities.ControlFlow;
import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.ActivityNode;
import com.nomagic.uml2.ext.magicdraw.actions.mdbasicactions.OpaqueAction;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdcommunications.TimeEvent;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralString;

import javax.annotation.CheckForNull;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.io.*;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

/**
 * Generate XML plugin
 */
@SuppressWarnings({"squid:S106", "squid:S1148", "UnusedDeclaration"})
public class MyPlugin5 extends Plugin
{
    /**
     * Action, that do all the work.
     */
    private ShowStatistics statsAction;

    /**
     * Hierarchy visitor, for counting elements.
     * Can do different actions with different element types.
     */
    private final ModelHierarchyVisitor visitor = new ModelHierarchyVisitor();

    /**
     * Build a document object to operate XML
     */
    Document document = DocumentHelper.createDocument();
    String XMLFileName = "XML4MagicDraw";  //XML file name

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
            super("", "Generate XML", null, null);
        }

        /**
         * Performs new count.
         * Clear results map, starts new counter and displays results.
         */
        @Override
        public void actionPerformed(ActionEvent evt)
        {
            // count children of selected in browser element.
            Tree tree = getTree();
            if (tree != null)
            {
                final Element element = getSelectedElement(tree);
                if (element != null)
                {
                    visitChildren(element);
                }
                JOptionPane.showMessageDialog(null, "Success!", "XML Generation", JOptionPane.PLAIN_MESSAGE);
            }

            // Format
            OutputFormat format = OutputFormat.createPrettyPrint();
            format.setEncoding("UTF-8");

            /*
             * Generate XML file
             * The file is located in the root path of the project
             */
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try
            {
                XMLWriter writer = new XMLWriter(out, format);
                writer.write(document);
                writer.close();
            }
            catch (IOException e)
            {
                System.out.println("File generation failed! File name: " + XMLFileName);
            }
            try (FileOutputStream fos = new FileOutputStream(XMLFileName + ".xml"))
            {
                fos.write(out.toByteArray());
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
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
         * @param root the root model element.
         */
        public void visitChildren(Element root)
        {
            ArrayList<Element> all = new ArrayList<>();
            all.add(root);

            // Get the root model element information
            String rootID = root.getID();
            String rootClassType = ClassTypes.getShortName(root.getClassType());
            String rootHumanName = root.getHumanName();
            // Write into XML file
            document.clearContent();
            org.dom4j.Element rootItem = document.addElement(rootClassType).addAttribute("id", rootID)
                                                                           .addAttribute("humanname", rootHumanName);

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
                Collection<Element> childrenElement = current.getOwnedElement();
                all.addAll(childrenElement);

                for(Element childElement: childrenElement)
                {
                    // Get the  childElement's parent element information
                    String childParentID = childElement.getObjectParent().getID();
                    String childParentClassType = ClassTypes.getShortName(childElement.getObjectParent().getClassType());

                    // if childElement's parent element is the root
                    if(Objects.equals(childParentID, rootID))
                    {
                        // Get the child model element information
                        String childID = childElement.getID();
                        String childClassType = ClassTypes.getShortName(childElement.getClassType());
                        String childHumanName = childElement.getHumanName();
                        // Write into XML file
                        org.dom4j.Element childItem = rootItem.addElement(childClassType).addAttribute("id", childID)
                                                                                         .addAttribute("humanname", childHumanName);
                    }
                    else
                    {
                        // Get all the context under the root Node
                        String select1 = "/" + rootClassType;
                        String select2 = "//" + childParentClassType + "[@id=" + "'" + childParentID + "'" + "]";

                        org.dom4j.Node rootNode = document.selectSingleNode(select1);
                        List<org.dom4j.Node> childParent = rootNode.selectNodes(select2);
                        org.dom4j.Element childParentItem = (org.dom4j.Element) childParent.get(0);

                        String childClassType = ClassTypes.getShortName(childElement.getClassType());
                        switch(childClassType)
                        {
                            case "ControlFlow":
                                ControlFlow elementControlFlow = (ControlFlow)childElement;
                                writeControlFlow2XML(childParentItem, elementControlFlow);
                                break;

                            case "OpaqueAction":
                                OpaqueAction elementOpaqueAction = (OpaqueAction)childElement;
                                writeOpaqueAction2XML(childParentItem, elementOpaqueAction);
                                break;

                            case "LiteralUnlimitedNatural":
                                LiteralUnlimitedNatural elementLiteralUnlimitedNatural = (LiteralUnlimitedNatural)childElement;
                                writeLiteralUnlimitedNatural2XML(childParentItem, elementLiteralUnlimitedNatural);
                                break;

                            case "Trigger":
                                Trigger elementTrigger = (Trigger)childElement;
                                writeTrigger2XML(childParentItem, elementTrigger);
                                break;

                            default:
                                writeElement2XML(childParentItem, childElement);
                        }
                    }
                }
            }
        }

        /*
         * For different types of elements,
         * add the unique attributes to the XML statement
         */
        public void writeElement2XML(org.dom4j.Element root, Element element)
        {
            // Get the child model element information
            String elementID = element.getID();
            String elementClassType = ClassTypes.getShortName(element.getClassType());
            String elementHumanName = element.getHumanName();

            // Write into XML file
            org.dom4j.Element item = root.addElement(elementClassType).addAttribute("id", elementID)
                                                                      .addAttribute("humanname", elementHumanName);
        }

        // ControlFlow
        public void writeControlFlow2XML(org.dom4j.Element root, ControlFlow element)
        {
            // ControlFlow element has source and target, get them
            ActivityNode source = element.getSource();
            ActivityNode target = element.getTarget();

            // Get all the  element information
            String elementID = element.getID();
            String elementClassType = ClassTypes.getShortName(element.getClassType());
            String elementHumanName = element.getHumanName();
            String sourceID = null;
            String targetID = null;
            if (source != null)
            {
                sourceID = source.getID();
            }
            if (target != null)
            {
                targetID = target.getID();
            }

            // Write into XML file
            org.dom4j.Element item = root.addElement(elementClassType).addAttribute("id", elementID)
                                                                      .addAttribute("humanname", elementHumanName)
                                                                      .addAttribute("source", sourceID)
                                                                      .addAttribute("target", targetID);
        }


        // OpaqueAction
        public void writeOpaqueAction2XML(org.dom4j.Element root, OpaqueAction element)
        {
            // Get all the  element information
            List<String> body, language;
            String elementBody = null;
            String elementLanguage = null;
            String elementID = element.getID();
            String elementClassType = ClassTypes.getShortName(element.getClassType());
            String elementHumanName = element.getHumanName();

            // OpaqueAction element has body and language, get them
            boolean hasBody = element.hasBody();
            boolean hasLanguage = element.hasLanguage();
            if(hasBody)
            {
                body = element.getBody();
                elementBody = StringUtils.join(body, ";");
            }
            if(hasLanguage)
            {
                language = element.getLanguage();
                elementLanguage = StringUtils.join(language, ";");
            }

            // Write into XML file
            org.dom4j.Element item = root.addElement(elementClassType).addAttribute("id", elementID)
                                                                      .addAttribute("humanname", elementHumanName)
                                                                      .addAttribute("body", elementBody)
                                                                      .addAttribute("language", elementLanguage);
        }

        // LiteralUnlimitedNatural
        public void writeLiteralUnlimitedNatural2XML(org.dom4j.Element root, LiteralUnlimitedNatural element)
        {
            // Get all the  element information
            String elementID = element.getID();
            String elementClassType = ClassTypes.getShortName(element.getClassType());
            String elementHumanName = element.getHumanName();
            String value = String.valueOf(element.getValue());

            // Write into XML file
            org.dom4j.Element item = root.addElement(elementClassType).addAttribute("id", elementID)
                                                                      .addAttribute("humanname", elementHumanName)
                                                                      .addAttribute("value", value);
        }

        // Trigger
        public void writeTrigger2XML(org.dom4j.Element root, Trigger element)
        {
            // Get all the  element information
            String elementID = element.getID();
            String elementClassType = ClassTypes.getShortName(element.getClassType());
            String elementHumanName = element.getHumanName();
            String elementEventType = ClassTypes.getShortName(Objects.requireNonNull(element.getEvent()).getClassType());

            // Write into XML file
            org.dom4j.Element item = root.addElement(elementClassType).addAttribute("id", elementID)
                                                                      .addAttribute("humanname", elementHumanName)
                                                                      .addAttribute("event", elementEventType);

            /*
             * For different types of events,
             * add the unique attributes to the XML statement
             */
            if(Objects.equals(elementEventType, "TimeEvent"))
            {
                TimeEvent timeEvent = (TimeEvent)element.getEvent();
                TimeExpression when = timeEvent.getWhen();
                String time = null;
                if(when != null)
                {
                    LiteralString expr = (LiteralString)when.getExpr();
                    if(expr != null)
                    {
                        time = expr.getValue();
                    }
                }
                item.addAttribute("when", time);
            }
        }
    }
}
