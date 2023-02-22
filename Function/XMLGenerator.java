package plugin.Function;

import com.nomagic.magicdraw.ui.browser.*;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.ui.browser.actions.DefaultBrowserAction;
import com.nomagic.magicdraw.uml.ClassTypes;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdcommunications.Trigger;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdsimpletime.TimeExpression;
import com.nomagic.uml2.impl.ModelHierarchyVisitor;

import com.nomagic.uml2.ext.magicdraw.activities.mdbasicactivities.ControlFlow;
import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.ActivityNode;
import com.nomagic.uml2.ext.magicdraw.actions.mdbasicactions.OpaqueAction;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdcommunications.TimeEvent;

import javax.annotation.CheckForNull;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;

import Producer.Producer;

/**
 * Action which displays its name.
 *
 * @author Donatas Simkunas
 */
public class XMLGenerator extends DefaultBrowserAction
{

    /**
     * Hierarchy visitor, for counting elements.
     * Can do different actions with different element types.
     */
    private final ModelHierarchyVisitor visitor = new ModelHierarchyVisitor();

    /**
     * User information
     */
    String userID = "B19080618";
    String modelType = "MagicDraw";
    boolean update = false;
    User user = new User(userID, modelType, update);

    /**
     * Build a document object to operate XML
     */
    Document document = DocumentHelper.createDocument();
    String docName = user.getUserID() + " " + user.getModelType() + " " + user.getDate();  //XML file name

    public XMLGenerator()
    {
        super("", "Generate XML file", null, null);
    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent evt)
    {
        // count children of selected in browser element.
        Browser browser = Application.getInstance().getMainFrame().getBrowser();
        if (browser != null)
        {
            // BrowserTabTree tree = browser.getActiveTree();
            ContainmentTree tree = browser.getContainmentTree();
            final Element element;
            if (tree != null)
            {
                document.clearContent();
                org.dom4j.Element userItem = writeUserInf2XML(user);

                element = getSelectedElement(tree);
                if (element != null)
                {
                    visitChildren(element, userItem);

                    try
                    {
                        // Send
                        String QUEUE_NAME = "hello";
                        String host = "8.130.42.107";
                        int port = 5672;
                        String virtualHost = "/";
                        String userName = "admin";
                        String userPassword = "123456";

                        Producer producer = new Producer(QUEUE_NAME, docName, document, host, port,
                                                         virtualHost, userName, userPassword, userID, modelType);
                        producer.send();
                    }
                    catch (Exception e)
                    {
                        throw new RuntimeException(e);
                    }
                }
                else
                {
                    JOptionPane.showMessageDialog(null, "The root is NULL!", "XML Generation", JOptionPane.PLAIN_MESSAGE);
                }
            }
            else
            {
                JOptionPane.showMessageDialog(null, "The tree is NULL!", "XML Generation", JOptionPane.PLAIN_MESSAGE);
            }
        }
        else
        {
            JOptionPane.showMessageDialog(null, "Error!", "XML Generation", JOptionPane.PLAIN_MESSAGE);
        }
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

    public void visitChildren(Element root, org.dom4j.Element rootItem)
    {
        ArrayList<Element> all = new ArrayList<>();
        all.add(root);

        // Get the root model element information
        String rootID = root.getID();
        String rootClassType = ClassTypes.getShortName(root.getClassType());
        String rootHumanType = root.getHumanType();
        String rootName = ((NamedElement)root).getName();
        // Write into XML file
        org.dom4j.Element magicDrawRootItem = rootItem.addElement(rootClassType).addAttribute("id", rootID)
                                                                                .addAttribute("type", rootHumanType)
                                                                                .addAttribute("name", rootName);

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
                    String childHumanType = childElement.getHumanType();
                    String childName = ((NamedElement)childElement).getName();
                    // Write into XML file
                    org.dom4j.Element childItem = magicDrawRootItem.addElement(childClassType).addAttribute("id", childID)
                                                                                              .addAttribute("type", childHumanType)
                                                                                              .addAttribute("name", childName);
                }
                else
                {
                    // Get all the context under the root Node
                    String select1 = "/User/" + rootClassType;
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

                        case "StringTaggedValue":
                            StringTaggedValue elementStringTaggedValue = (StringTaggedValue)childElement;
                            writeStringTaggedValue2XML(childParentItem, elementStringTaggedValue);
                            break;

                        default:
                            writeElement2XML(childParentItem, childElement);
                    }
                }
            }
        }
    }

    /**
     * Write user information to XML file
     */
    public org.dom4j.Element writeUserInf2XML(User user)
    {
        String userID = user.getUserID();
        String modelType = user.getModelType();
        String update = String.valueOf(user.getUpdate() ? 1: 0);
        String date = user.getDate();

        // Write to XML file
        org.dom4j.Element userItem = document.addElement("User").addAttribute("id", userID)
                                                                   .addAttribute("modeltype", modelType)
                                                                   .addAttribute("update", update)
                                                                   .addAttribute("date", date);
        return userItem;
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
        String elementHumanType = element.getHumanType();
        String elementName = ((NamedElement)element).getName();

        // Write into XML file
        org.dom4j.Element item = root.addElement(elementClassType).addAttribute("id", elementID)
                                                                  .addAttribute("type", elementHumanType)
                                                                  .addAttribute("name", elementName);
    }

    // StringTaggedValue
    public void writeStringTaggedValue2XML(org.dom4j.Element root, StringTaggedValue element)
    {
        // Get the child model element information
        String elementID = element.getID();
        String elementClassType = ClassTypes.getShortName(element.getClassType());
        String elementHumanName = element.getHumanName();

        // Write into XML file
        org.dom4j.Element item = root.addElement(elementClassType).addAttribute("id", elementID)
                                                                  .addAttribute("type", elementHumanName);
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
        String elementHumanType = element.getHumanType();
        String elementName = element.getName();
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
                                                                  .addAttribute("type", elementHumanType)
                                                                  .addAttribute("name", elementName)
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
        String elementHumanType = element.getHumanType();
        String elementName = element.getName();

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
                                                                  .addAttribute("type", elementHumanType)
                                                                  .addAttribute("name", elementName)
                                                                  .addAttribute("body", elementBody)
                                                                  .addAttribute("language", elementLanguage);
    }

    // LiteralUnlimitedNatural
    public void writeLiteralUnlimitedNatural2XML(org.dom4j.Element root, LiteralUnlimitedNatural element)
    {
        // Get all the  element information
        String elementID = element.getID();
        String elementClassType = ClassTypes.getShortName(element.getClassType());
        String elementHumanType = element.getHumanType();
        String elementName = element.getName();
        String weight = String.valueOf(element.getValue());

        // Write into XML file
        org.dom4j.Element item = root.addElement(elementClassType).addAttribute("id", elementID)
                                                                  .addAttribute("type", elementHumanType)
                                                                  .addAttribute("name", elementName)
                                                                  .addAttribute("value", weight);
    }

    // Trigger
    public void writeTrigger2XML(org.dom4j.Element root, Trigger element)
    {
        // Get all the  element information
        String elementID = element.getID();
        String elementClassType = ClassTypes.getShortName(element.getClassType());
        String elementHumanType = element.getHumanType();
        String elementName = element.getName();
        String elementEventType = ClassTypes.getShortName(Objects.requireNonNull(element.getEvent()).getClassType());

        // Write into XML file
        org.dom4j.Element item = root.addElement(elementClassType).addAttribute("id", elementID)
                                                                  .addAttribute("type", elementHumanType)
                                                                  .addAttribute("name", elementName)
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
