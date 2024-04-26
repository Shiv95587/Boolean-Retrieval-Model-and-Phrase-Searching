package org.example;
import opennlp.tools.stemmer.PorterStemmer;
import opennlp.tools.stemmer.Stemmer;
import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class SearchEngine extends JFrame {

    //    Panels
    public static JPanel proximityPanel; // Declare the proximity panel outside the actionPerformed method
    JPanel referencePanel;
    JPanel mainPanel;
    JPanel panel1;
    JPanel panel2;
    JPanel panel3;
    JPanel searchPanel;

    // List Model and Document List
    public static DefaultListModel<String> listModel = new DefaultListModel<>();
    JList<String> referenceList;
    JScrollPane scrollPane;

    // Combo boxes options
    String[] notOptions = {"", "NOT"};
    String[] logicalOperators = {"AND", "OR"};
    String[] searchTypes = {"Boolean Retrieval Model", "Proximity Search"};

    public ArrayList<Long> documents = new ArrayList<>();

    ArrayList<String> stopWords = new ArrayList<>();

    static InvertedIndex invertedIndex = new InvertedIndex();
    static PositionalIndex positionalIndex = new PositionalIndex();
    static Map<String, Long> documentIDs = new HashMap<>();
    GridBagConstraints gbc;

    public SearchEngine() {

        this.setTitle("Shiv's Search Engine");
        this.setSize(1000, 400);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // This method creates the GUI
        createMainPage();

        String path = "ResearchPapers/";
        File directory = new File(path);
        File[] files = directory.listFiles();

        // Reading stopwords
        String fileName = "Stopword-List.txt"; // Replace with your file name
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Process each line here
                stopWords.add(line);
            }
        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
        }
        System.out.println("Stop words are: ");
        System.out.println(stopWords);

        if (files != null) {
            //  Reads the files and apply preprocessing techniques tokenizing,casefolding, stemming
            preprocess(path, files,stopWords);
        }
        searchTypeComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox) e.getSource();
                String selectedType = (String) cb.getSelectedItem();
                if ("Boolean Retrieval Model".equals(selectedType)) {
                    // Show original search panel
                    panel1.setVisible(true);
                    panel2.setVisible(true);
                    panel3.setVisible(true);
                    searchField1.setVisible(true);
                    searchField2.setVisible(true);
                    searchField3.setVisible(true);
                    notOperation1.setVisible(true);
                    notOperation2.setVisible(true);
                    notOperation3.setVisible(true);
                    logicalOperator1.setVisible(true);
                    logicalOperator2.setVisible(true);
                    searchButton.setVisible(true);

                    // Remove proximity panel if exists
                    if (proximityPanel != null) {
                        searchPanel.remove(proximityPanel);
                        proximityPanel = null;
                    }
                } else if ("Proximity Search".equals(selectedType)) {
                    // Hide original search panel and show proximity search panel
                    panel1.setVisible(false);
                    panel2.setVisible(false);
                    panel3.setVisible(false);
                    searchField1.setVisible(false);
                    searchField2.setVisible(false);
                    searchField3.setVisible(false);
                    notOperation1.setVisible(false);
                    notOperation2.setVisible(false);
                    notOperation3.setVisible(false);
                    logicalOperator1.setVisible(false);
                    logicalOperator2.setVisible(false);
                    searchButton.setVisible(false);

                    // Remove proximity panel if exists
                    if (proximityPanel != null) {
                        searchPanel.remove(proximityPanel);
                    }

                    // Create and show proximity search panel
                    proximityPanel = createProximityPanel();
                    gbc.gridx = 0;
                    gbc.gridy = 0; // Update the row index to avoid overlapping
                    gbc.gridwidth = 5;
                    gbc.insets = new Insets(10, 10, 10, 10);
                    searchPanel.add(proximityPanel, gbc);
                }
                revalidate();
                repaint();
            }
        });

//      -----------------Action Listeners--------------------
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String term1 = searchField1.getText().trim();
                String term2 = searchField2.getText().trim();
                String term3 = searchField3.getText().trim();
                ArrayList<Long> results = new ArrayList<>();

                // Check if search fields are filled from left to right and adjacently
                if (term1.isEmpty()) {
                    showErrorDialog("Missing Input", "Please fill the first search field.");
                } else if (term2.isEmpty() && !term3.isEmpty()) {
                    showErrorDialog("Invalid Input", "Please fill the second search field.");
                } else if (term2.isEmpty() && term3.isEmpty()) {
                    // Only the first field is filled
                    LinkedList l =  processOneTermQuery(term1);
                    if(l != null) {
                        results = l.convertToArrayList();
                    }
                    else results = null;
                    System.out.println(results);
                } else if (!term2.isEmpty() && term3.isEmpty()) {
                    // First and second fields are filled
                    LinkedList l = processTwoTermQuery(term1,term2);
                    if(l != null) {
                        results = l.convertToArrayList();
                    }
                    else results = null;
                    System.out.println(results);
                    System.out.println("Search with term1: " + term1 + ", term2: " + term2);
                } else {
                    // All three fields are filled
                    term1 = preprocessQueryTerm(term1);
                    term2 = preprocessQueryTerm(term2);
                    term3 = preprocessQueryTerm(term3);
                    LinkedList l = processThreeTermQuery(term1,term2, term3);
                    if(l != null) {
                        results = l.convertToArrayList();
                    }
                    else results = null;
                    System.out.println("Search with term1: " + term1 + ", term2: " + term2 + ", term3: " + term3);
                }
                updateResultList(results);
            }
        });

        proximitySearchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ArrayList<Long> results = null;
                String term1 = proximitySearchField1.getText();

                String term2 = proximitySearchField3.getText();

                term1 = preprocessQueryTerm(term1);
                term2 = preprocessQueryTerm(term2);

                System.out.println(term1);
                System.out.println(term2);
                if(term1.isEmpty() || term2.isEmpty() || proximitySearchField2.getText().isEmpty())
                {
                    showErrorDialog("Missing Input", "Please fill all the search fields.");
                }
                else
                {
                    int distance = Integer.parseInt(proximitySearchField2.getText());
                    if (!positionalIndex.index.containsKey(term1) || !positionalIndex.index.containsKey(term2)) {
                        results = null;
                    }
                    else {
                        results = positionalIndex.positionalIntersect(positionalIndex.index.get(term1), positionalIndex.index.get(term2), distance);
                    }
                    System.out.println(results);
                    updateResultList(results);
                }

            }
        });
        this.setVisible(true);
    }

    //-------------Query Processing methods-----------
    public LinkedList processOneTermQuery(String term)
    {
        term = preprocessQueryTerm(term);
        System.out.println("Search with term1: " + term);
        LinkedList l = null;
        if(invertedIndex.index.containsKey(term)) {
            l = invertedIndex.index.get(term);
            l.display();
        }
            if("NOT".equals((String) notOperation1.getSelectedItem()))
                return LinkedList.operationNOT(l,new ArrayList<Long>(documentIDs.values()));
            else
                return l;
    }
    public LinkedList processTwoTermQuery(String term1, String term2)
    {
        term1 = preprocessQueryTerm(term1);
        term2 = preprocessQueryTerm(term2);
        LinkedList list1 = null , list2 = null;
        if(invertedIndex.index.containsKey(term1))
            list1 = invertedIndex.index.get(term1);
        if(invertedIndex.index.containsKey(term2))
            list2 = invertedIndex.index.get(term2);

        if("NOT".equals((String) notOperation1.getSelectedItem()))
        {
            list1 = LinkedList.operationNOT(list1,new ArrayList<Long>(documentIDs.values()));
        }
        if("NOT".equals((String) notOperation2.getSelectedItem()))
        {
            list2 = LinkedList.operationNOT(list2,new ArrayList<Long>(documentIDs.values()));
        }
        if("AND".equals((String) logicalOperator1.getSelectedItem()))
        {
            return LinkedList.intersect(list1, list2);
        }
        else
        {
            return LinkedList.operationOR(list1, list2);
        }
    }
    public LinkedList processThreeTermQuery(String term1, String term2, String term3)
    {
        LinkedList l = processTwoTermQuery(term1, term2);
        if("AND".equals((String) logicalOperator2.getSelectedItem()))
        {
            if(l == null)
                return null;
            else {
                LinkedList list2 = null;
                if(invertedIndex.index.containsKey(term3))
                    list2 = invertedIndex.index.get(term3);

                if("NOT".equals((String) notOperation3.getSelectedItem()))
                {
                    list2 = LinkedList.operationNOT(list2,new ArrayList<Long>(documentIDs.values()));
                }
                return LinkedList.intersect(l,list2);
            }
        }
        else {
            LinkedList list2 = null;
            if(invertedIndex.index.containsKey(term3))
                list2 = invertedIndex.index.get(term3);
            if("NOT".equals((String) notOperation3.getSelectedItem()))
                list2 = LinkedList.operationNOT(list2,new ArrayList<Long>(documentIDs.values()));
            if(l == null)
            {
                return list2;
            }
            else {
                return LinkedList.operationOR(l, list2);
            }
        }
    }

    // Updates the documents List view
    public void updateResultList(ArrayList<Long> results)
    {
        listModel.clear();
        if(results == null || results.size() == 0) {
            listModel.addElement(String.valueOf("Your search did not match any documents."));
            return;
        }
        for(long result : results)
        {
            listModel.addElement(String.valueOf(result));
        }
    }

//    pre-processing methods
    public static String preprocessQueryTerm(String term)
    {
        PorterStemmer stemmer = new PorterStemmer();
        return stemmer.stem(term.toLowerCase());
    }
    public static void preprocess(String path, File[] files, ArrayList<String> stopWords) {
        for (File file : files) {
            if (file.isFile()) {
                String s = file.getName().split("\\.")[0];
                documentIDs.put(file.getName(), Long.parseLong(s));
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    String line;
                    ArrayList<String> fileTokens = new ArrayList<>();
                    while ((line = reader.readLine()) != null) {
                        ArrayList<String> lineTokens = new ArrayList<>(Arrays.asList(line.split("[^a-zA-Z]+")));
                        fileTokens.addAll(lineTokens);
                    }
                    caseFold(fileTokens);
                    stem(fileTokens);
                    long d = documentIDs.get(file.getName());
                    positionalIndex.addDocument(fileTokens, d);
                    fileTokens.removeAll(stopWords);
                    stem(fileTokens);
                    invertedIndex.insertDocument(fileTokens, d);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    // stems the list of tokens using porter stemmer
    private static void stem(ArrayList<String> tokens) {
        PorterStemmer stemmer = new PorterStemmer();
        for (int i = 0; i < tokens.size(); ++i) {
            String token = tokens.get(i);
            tokens.set(i, stemmer.stem(token));
        }
    }

    // lowercase list of tokens
    private static void caseFold(ArrayList<String> tokens) {
        for (int i = 0; i < tokens.size(); ++i) {
            String token = tokens.get(i);
            tokens.set(i, token.toLowerCase());
        }
    }
    // end preprocessing methods


//  --------------------GUI Creation Methods------------------------
    public void createMainPage()
    {
        // Search Type Combo Box
        searchTypeComboBox = new JComboBox<>(searchTypes);
        searchTypeComboBox.setSelectedIndex(0); // Default selection
        searchTypeComboBox.setBackground(Color.white);

        // Search Panel
        searchPanel  = new JPanel(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5); // Add some space between components

        panel1 =  new JPanel(new FlowLayout());
        panel2 = new JPanel(new FlowLayout());
        panel3 = new JPanel(new FlowLayout());
        searchField1 = new JTextField(10);
        searchField2 = new JTextField(10);
        searchField3 = new JTextField(10);

        // Initialize JComboBox for logical operators (AND or OR)
        logicalOperator1 = new JComboBox<>(logicalOperators);
        logicalOperator2 = new JComboBox<>(logicalOperators);
        logicalOperator1.setBackground(Color.white);
        logicalOperator2.setBackground(Color.white);

        // Initialize JComboBox for NOT operation with options
        notOperation1 = new JComboBox<>(notOptions);
        notOperation2 = new JComboBox<>(notOptions);
        notOperation3 = new JComboBox<>(notOptions);
        notOperation1.setBackground(Color.white);
        notOperation2.setBackground(Color.white);
        notOperation3.setBackground(Color.white);
        // Remove down arrow from combo boxes
        notOperation1.setUI(new BasicComboBoxUI());
        notOperation2.setUI(new BasicComboBoxUI());
        notOperation3.setUI(new BasicComboBoxUI());
        logicalOperator1.setUI(new BasicComboBoxUI());
        logicalOperator2.setUI(new BasicComboBoxUI());

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel1.add(notOperation1);
        panel1.add(searchField1);
        searchPanel.add(panel1, gbc);
        gbc.insets = new Insets(5, 20, 5, 5); // Add more space between logical operators and search fields
        gbc.gridx = 1;
        gbc.gridy = 0;
        searchPanel.add(logicalOperator1, gbc);
        gbc.gridx = 2;
        gbc.gridy = 0;
        panel2.add(notOperation2);
        panel2.add(searchField2);
        searchPanel.add(panel2, gbc);
        gbc.gridx = 3;
        gbc.gridy = 0;
        searchPanel.add(logicalOperator2, gbc);
        gbc.gridx = 4;
        gbc.gridy = 0;
        panel3.add(notOperation3);
        panel3.add(searchField3);
        searchPanel.add(panel3, gbc);

        searchButton = new JButton("Search");
        searchButton.setBackground(new Color(255, 255, 255));
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        searchPanel.add(searchButton, gbc);

        // Add search type combo box
        gbc.gridx = 5;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 20, 5, 5);
        searchPanel.add(searchTypeComboBox, gbc);

        // Document List Panel
        referencePanel = new JPanel();
        referencePanel.setLayout(new BorderLayout());

        // Document Links
        referenceList = new JList<>(listModel);
        referenceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scrollPane = new JScrollPane(referenceList);
        referencePanel.add(new JLabel("Documents"), BorderLayout.NORTH);
        referencePanel.add(scrollPane, BorderLayout.CENTER);

        // Main Panel
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(searchPanel, BorderLayout.NORTH);
        mainPanel.add(referencePanel, BorderLayout.CENTER);
        add(mainPanel);
    }

    private JPanel createProximityPanel() {
        JPanel proximityPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 20, 5, 5); // Add some space between components


        proximitySearchButton.setBackground(Color.white);
        gbc.gridx = 0;
        gbc.gridy = 0;
        proximityPanel.add(proximitySearchField1, gbc);
        gbc.gridx = 2;
        gbc.gridy = 0;
        proximityPanel.add(proximitySearchField3, gbc);
        gbc.gridx = 4;
        gbc.gridy = 0;
        JPanel distancePanel = new JPanel();
        JLabel slashLabel = new JLabel("/");
        slashLabel.setFont(slashLabel.getFont().deriveFont(Font.BOLD, 18));
        distancePanel.add(slashLabel);
        distancePanel.add(proximitySearchField2);
        proximityPanel.add(distancePanel,gbc);
//        proximityPanel.add(proximitySearchField2, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 6;
        proximityPanel.add(proximitySearchButton, gbc);
        return proximityPanel;
    }
    public static void showErrorDialog(String title, String message) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
    }
}
