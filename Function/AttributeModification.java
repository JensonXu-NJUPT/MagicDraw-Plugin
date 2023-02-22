package plugin.Function;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.ui.browser.Browser;
import com.nomagic.magicdraw.ui.browser.ContainmentTree;
import com.nomagic.magicdraw.ui.browser.Node;
import com.nomagic.magicdraw.ui.browser.Tree;
import com.nomagic.magicdraw.uml.ClassTypes;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralUnlimitedNatural;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdsimpletime.TimeExpression;
import com.nomagic.uml2.impl.ModelHierarchyVisitor;

import com.nomagic.uml2.ext.magicdraw.activities.mdbasicactivities.ControlFlow;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdcommunications.TimeEvent;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralString;

import javax.annotation.CheckForNull;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.*;
/**
 * Action which displays its name.
 *
 * @author Donatas Simkunas
 */
public class AttributeModification extends MDAction
{
    /**
     * Hierarchy visitor, for counting elements.
     * Can do different actions with different element types.
     */
    private final ModelHierarchyVisitor visitor = new ModelHierarchyVisitor();

    public AttributeModification()
    {
        super("", "Modify Attribute Value", null, null);
    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e)
    {
        Browser browser = Application.getInstance().getMainFrame().getBrowser();
        if(browser != null)
        {
            ContainmentTree tree = browser.getContainmentTree();
            if(tree != null)
            {
                final Element element = getSelectedElement(tree);
                if(element != null)
                {
                    ArrayList<Element> allElements = getAllElement(element);

                    // JOptionPane.showMessageDialog(null, "Program has been start.", "Tips", JOptionPane.PLAIN_MESSAGE);
                    String message = "Please choose the ClassType of the element which need to be modified";
                    String title = "Attribute Modification";
                    String[] options = {"Trigger", "ControlFlow"};
                    int option = JOptionPane.showOptionDialog(null, message, title, JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, "Trigger");

                    switch(option)
                    {
                        case 0:
                            setTriggerWhen(allElements);
                            break;
                        case 1:
                            setControlFlowWeight(allElements);
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

    /**
     * Goes through all children of given model elements.
     * Demonstrates way how to collect all children by using FOR cycle and avoiding recursion.
     * Visit every child with StatisticsVisitor.
     *
     * @param root the root model element.
     */
    public ArrayList<Element> getAllElement(Element root)
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
            } catch (Exception e)
            {
                e.printStackTrace();
            }
            // add all children into end of this list, so it emulates recursion.
            Collection<Element> childrenElement = current.getOwnedElement();
            all.addAll(childrenElement);
        }
        return all;
    }

    // Determine if the element exists
    public boolean isExistElement(ArrayList<Element> elements, String ID)
    {
        boolean isExist = false;
        for(int i = 0; i < elements.size(); i++)
        {
            Element element = elements.get(i);
            if(Objects.equals(ID, element.getID()))
            {
                isExist = true;
                break;
            }
        }
        return isExist;
    }

    // Find the element corresponding to the ID
    public Element findElement(ArrayList<Element> elements, String ID)
    {
        Element element = null;
        for(int i = 0; i < elements.size(); i++)
        {
            Element e = elements.get(i);
            if(Objects.equals(ID, e.getID()))
            {
                element = e;
                break;
            }
        }
        return element;
    }

    // Change the value of When in TimeEvent Trigger
    public void setTriggerWhen(ArrayList<Element> elements)
    {
        String message1 = "Please input Element ID";
        String title = "Trigger";
        String ID = JOptionPane.showInputDialog(null, message1, title, JOptionPane.QUESTION_MESSAGE);

        boolean isExist = isExistElement(elements, ID);
        if(isExist)
        {
            Element element = findElement(elements, ID);
            String elementEventType = ClassTypes.getShortName(element.getClassType());
            if(Objects.equals(elementEventType, "TimeEvent"))
            {
                TimeEvent timeEvent = (TimeEvent)element;
                TimeExpression when = timeEvent.getWhen();
                if(when != null)
                {
                    String message2 = "Please input the new value of When";
                    String time = JOptionPane.showInputDialog(null, message2, title, JOptionPane.QUESTION_MESSAGE);

                    LiteralString expr = (LiteralString)when.getExpr();
                    if(expr != null)
                    {
                        expr.setValue(time);
                        JOptionPane.showMessageDialog(null, "Success!", title, JOptionPane.PLAIN_MESSAGE);
                    }
                }
            }
            else
            {
                String errorMessage = "Error: Type mismatch!";
                JOptionPane.showMessageDialog(null, errorMessage, title, JOptionPane.PLAIN_MESSAGE);
            }
        }
        else
        {
            String errorMessage = "Element doesn't exist!";
            JOptionPane.showMessageDialog(null, errorMessage, title, JOptionPane.PLAIN_MESSAGE);
        }
    }

    // Change the value of weight in ControlFlow
    public void setControlFlowWeight(ArrayList<Element> elements)
    {
        String message1 = "Please input Element ID";
        String title = "ControlFlow";
        String ID = JOptionPane.showInputDialog(null, message1, title, JOptionPane.QUESTION_MESSAGE);

        boolean isExist = isExistElement(elements, ID);
        if(isExist)
        {
            ControlFlow element = (ControlFlow)findElement(elements, ID);
            String elementClassType = ClassTypes.getShortName(element.getClassType());
            if(Objects.equals(elementClassType, "ControlFlow"))
            {
                LiteralUnlimitedNatural edgeWeight = (LiteralUnlimitedNatural)element.getWeight();
                if(edgeWeight != null)
                {
                    String message2 = "Please input the new value of Weight";
                    String weight = JOptionPane.showInputDialog(null, message2, title, JOptionPane.QUESTION_MESSAGE);

                    edgeWeight.setValue(Integer.parseInt(weight));
                    JOptionPane.showMessageDialog(null, "Success!", title, JOptionPane.PLAIN_MESSAGE);
                }
            }
            else
            {
                String errorMessage = "Error: Type mismatch!";
                JOptionPane.showMessageDialog(null, errorMessage, title, JOptionPane.PLAIN_MESSAGE);
            }
        }
        else
        {
            String errorMessage = "Element doesn't exist!";
            JOptionPane.showMessageDialog(null, errorMessage, title, JOptionPane.PLAIN_MESSAGE);
        }
    }
}
