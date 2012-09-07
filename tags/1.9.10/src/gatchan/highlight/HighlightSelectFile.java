// funa class
// save, load dialog

package gatchan.highlight;

import org.gjt.sp.jedit.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;


class HighlightSelectFile {
  private JDialog selectDialog = null;
  private JList list = null;
  private DefaultListModel listModel = null;
  private JTextField tf = null;
  private File selectedFile = null;
  
  private static HighlightSelectFile instance  = null;
  
  public static HighlightSelectFile getInstance(){
    if (instance == null){
      instance = new HighlightSelectFile();
    }
    return instance;
  }
  
  
  public File showSelectDialog(){
    File result = null;
    if (selectDialog == null){
      selectDialog = createDialog();
    }
    
    listModel.clear();
    ArrayList<ListElement> fileList = getHighlightFiles();
    for (ListElement element : fileList){
      listModel.addElement(element);
    }
    tf.setText("");
    tf.requestFocus();
    if (listModel.getSize() > 0){
      list.setSelectedIndex(0);
    }
    selectedFile = null;
    selectDialog.pack();
    selectDialog.setLocationRelativeTo(jEdit.getActiveView());
    selectDialog.setVisible(true);
    
    result = selectedFile;
    return result;
  }
  
  private JDialog createDialog(){
    JDialog dialog = new JDialog(jEdit.getActiveView(), true);
    Container c = dialog.getContentPane();
    c.setLayout(new BorderLayout());
    tf = new JTextField();
    c.add(tf, BorderLayout.NORTH);
    listModel = new DefaultListModel();
    list = new JList(listModel);
    c.add(new JScrollPane(list), BorderLayout.CENTER);
    list.setSelectedIndex(ListSelectionModel.SINGLE_SELECTION );
    
    Action nextAction = new AbstractAction("next"){
      public void actionPerformed(ActionEvent e){
        int count = listModel.getSize();
        if (count < 1){
          return;
        }
        
        int index = list.getSelectedIndex();
        index = (index + 1) % count;
        list.setSelectedIndex(index);
      }
    };
    Action prevAction = new AbstractAction("prev"){
      public void actionPerformed(ActionEvent e){
        int count = listModel.getSize();
        if (count < 1){
          return;
        }
        
        int index = list.getSelectedIndex();
        index += count;
        index = (index - 1) % count;
        list.setSelectedIndex(index);
      }
    };
    final Action selectAction = new AbstractAction("select"){
      public void actionPerformed(ActionEvent e){
        String inputPath = tf.getText().trim();
        if (list.getSelectedIndex() >= 0){
          selectedFile = ((ListElement)list.getSelectedValue()).file;
        } else if (!"".equals(inputPath)) {
          selectedFile = new File(getHome(), inputPath);
        }
        selectDialog.setVisible(false);
        selectDialog.dispose();
      }
    };
    
    Action hideAction = new AbstractAction("hide"){
      public void actionPerformed(ActionEvent e){
        selectDialog.setVisible(false);
        selectDialog.dispose();
      }
    };
    
    KeyStroke escKeystroke = KeyStroke.getKeyStroke("ESCAPE");
    KeyStroke upKeystroke = KeyStroke.getKeyStroke("UP");
    KeyStroke downKeystroke = KeyStroke.getKeyStroke("DOWN");
    KeyStroke altiKeystroke = KeyStroke.getKeyStroke("alt I");
    KeyStroke altkKeystroke = KeyStroke.getKeyStroke("alt K");
    KeyStroke althKeystroke = KeyStroke.getKeyStroke("alt H");
    
    InputMap inputmap = tf.getInputMap(JTextField.WHEN_IN_FOCUSED_WINDOW);
    inputmap.put(downKeystroke, nextAction.getValue("Name"));
    inputmap.put(upKeystroke, prevAction.getValue("Name"));
    inputmap.put(escKeystroke, hideAction.getValue("Name"));
    inputmap.put(altkKeystroke, nextAction.getValue("Name"));
    inputmap.put(altiKeystroke, prevAction.getValue("Name"));
    inputmap.put(althKeystroke, hideAction.getValue("Name"));
    
    ActionMap actionmap = tf.getActionMap();
    actionmap.put(nextAction.getValue("Name"), nextAction);
    actionmap.put(prevAction.getValue("Name"), prevAction);
    actionmap.put(hideAction.getValue("Name"), hideAction);
    
    tf.addActionListener(selectAction);
    
    tf.addKeyListener(new KeyAdapter(){
        public void keyTyped(KeyEvent e){
          if ((e.getModifiers() & KeyEvent.ALT_MASK) != 0){
            return;
          }
          list.getSelectionModel().clearSelection();
        }
    });
    
    list.addMouseListener(new MouseAdapter()
      {
        public void mouseClicked(MouseEvent me)
        {
          if(me.getButton() == me.BUTTON1)
          {
            selectAction.actionPerformed(null);
          }
        }
      });
    
    return dialog;
  }
  
  // funa edit
  private File getHome(){
    String settingsDirectory = jEdit.getSettingsDirectory();
    if (settingsDirectory == null)
      return null;
    File file = new File(settingsDirectory, "plugins");
    String home = new File(file, HighlightPlugin.class.getName()).getPath();
    if (home == null)
      return null;
    
    File homeFolder = new File(home);
    if (!homeFolder.exists())
    {
      if (!homeFolder.mkdirs())
      {
        return null;
      }
    }
    if (!homeFolder.isDirectory() || !homeFolder.canWrite())
    {
      return null;
    }
    return homeFolder;
  }
  
  private ArrayList<ListElement> getHighlightFiles(){
    ArrayList<ListElement> files = new ArrayList<ListElement>();
    File home = getHome();
    if (home == null){
      return null;
    }
    
    for (File file : home.listFiles()){
      files.add(new ListElement(file));
    }
    
    return files;
  }
  
  private static class ListElement {
    private File file;
    
    private ListElement(File file){
      this.file = file;
    }
    
    @Override
    public String toString(){
      return file.getName();
    }
  }
  
}
