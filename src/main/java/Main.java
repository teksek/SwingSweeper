import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.jthemedetecor.OsThemeDetector;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;
import java.util.Random;
import java.util.prefs.Preferences;

public class Main {
    private static JFrame window;
    private static Preferences prefs;
    private static JPanel headerPanel;
    private static JPanel centerPanel;
    private static boolean firstClick = true;
    private static boolean isGameOver = false;
    private static Field[][] grid;
    private static JButton[][] buttons;
    private static int gameRows, gameCols, gameBombs;
    private static JLabel lblBombsLeft;
    private static JLabel lblTimer;
    private static Timer gameTimer;
    private static int secondsPassed;
    private static int flagsCount;
    private static String currentPreset = "S"; // S - mała, M - średnia, L - duża, Custom - własna
    private static final Color[] KOLORY_CYFR = {
            null,                                 // 0 — nieużywane
            new Color(88, 88, 251),      // 1 — niebieski
            new Color(0, 128, 0),        // 2 — zielony
            new Color(255, 0, 0),        // 3 — czerwony
            new Color(0, 0, 128),        // 4 — granatowy
            new Color(128, 0, 0),        // 5 — bordowy
            new Color(0, 128, 128),      // 6 — turkusowy
            new Color(0, 0, 0),          // 7 — czarny
            new Color(128, 128, 128),    // 8 — szary
    };

    private static void makeWindow() {
        gameTimer = new Timer(1000, e -> {
            secondsPassed++;
            if (lblTimer != null) {
                lblTimer.setText(String.format("⏱️ %03d", secondsPassed));
            }
        });

        setupBasicWindow();
        setupUI();
        finalizeWindowSetup();
    }

    private static void setupBasicWindow() {
        window = new JFrame("SwingSweeper");
        window.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        window.setIconImage(new ImageIcon(Objects.requireNonNull(Main.class.getResource("/icon.png"))).getImage()); //ustawienie ikony programu
        window.setJMenuBar(new AppMenuBar(prefs, window));
    }

    private static void setupUI() {
        headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setPreferredSize(new Dimension(0, 80));
        JLabel headerLabel = new JLabel("<html><center><font size='6'><b>SwingSweeper</b></font></center></html>");
        headerPanel.add(headerLabel);

        JPanel optionsPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        JLabel lblPreset = new JLabel("Poziom trudności:");
        JComboBox<String> comboPresets = new JComboBox<>(new String[]{
                "Mała (9x9, 10 bomb)",
                "Średnia (16x16, 40 bomb)",
                "Duża (16x30, 99 bomb)",
                "Własna"
        });
        JLabel lblIloscBomb = new JLabel("Ilość bomb:");
        JSpinner spinnerBomby = new JSpinner(new SpinnerNumberModel(10, 1, 2000, 1));
        JLabel lblKolumny = new JLabel("Kolumny:");
        JSpinner spinnerKolumny = new JSpinner(new SpinnerNumberModel(12, 6, 50, 1));
        JLabel lblWiersze = new JLabel("Rzędy:");
        JSpinner spinnerWiersze = new JSpinner(new SpinnerNumberModel(12, 6, 50, 1));

        comboPresets.addActionListener(e -> {
            int idx = comboPresets.getSelectedIndex();
            if (idx == 0) { // mała
                spinnerWiersze.setValue(9); spinnerKolumny.setValue(9); spinnerBomby.setValue(10);
                toggleSpinners(false, spinnerWiersze, spinnerKolumny, spinnerBomby);
                currentPreset = "S";
            } else if (idx == 1) { // średnia
                spinnerWiersze.setValue(16); spinnerKolumny.setValue(16); spinnerBomby.setValue(40);
                toggleSpinners(false, spinnerWiersze, spinnerKolumny, spinnerBomby);
                currentPreset = "M";
            } else if (idx == 2) { // duża
                spinnerWiersze.setValue(16); spinnerKolumny.setValue(30); spinnerBomby.setValue(99);
                toggleSpinners(false, spinnerWiersze, spinnerKolumny, spinnerBomby);
                currentPreset = "L";
            } else { // Custom
                toggleSpinners(true, spinnerWiersze, spinnerKolumny, spinnerBomby);
                currentPreset = "Custom";
            }
        });

        toggleSpinners(false, spinnerWiersze, spinnerKolumny, spinnerBomby);

        optionsPanel.add(lblPreset);      optionsPanel.add(comboPresets);
        optionsPanel.add(lblIloscBomb);   optionsPanel.add(spinnerBomby);
        optionsPanel.add(lblKolumny);     optionsPanel.add(spinnerKolumny);
        optionsPanel.add(lblWiersze);     optionsPanel.add(spinnerWiersze);

        JButton btnStart = new JButton("Start!");
        btnStart.addActionListener(e -> gameStart((int) spinnerBomby.getValue(), (int) spinnerKolumny.getValue(), (int) spinnerWiersze.getValue()));

        centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        centerPanel.setPreferredSize(new Dimension(500, 500));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        gbc.gridy = 0; gbc.insets = new Insets(0, 0, 12, 0);
        JLabel imgLabel = new JLabel(new ImageIcon(Objects.requireNonNull(Main.class.getResource("/icon-small.png"))));
        centerPanel.add(imgLabel, gbc);

        gbc.gridy = 1; gbc.insets = new Insets(0, 40, 12, 40);
        centerPanel.add(optionsPanel, gbc);

        // --- LEADERBOARD ---
        int scoreS = prefs.getInt("highscore_S", 9999);
        int scoreM = prefs.getInt("highscore_M", 9999);
        int scoreL = prefs.getInt("highscore_L", 9999);
        String txtS = scoreS == 9999 ? "---" : scoreS + "s";
        String txtM = scoreM == 9999 ? "---" : scoreM + "s";
        String txtL = scoreL == 9999 ? "---" : scoreL + "s";

        gbc.gridy = 2; gbc.insets = new Insets(5, 0, 15, 0);
        JLabel lblScores = new JLabel("<html><center>🏆 <b>NAJLEPSZE WYNIKI</b> 🏆<br>Mała: " + txtS + " | Średnia: " + txtM + " | Duża: " + txtL + "</center></html>");
        lblScores.setHorizontalAlignment(SwingConstants.CENTER);
        centerPanel.add(lblScores, gbc);

        gbc.gridy = 3; gbc.insets = new Insets(0, 0, 0, 0);
        gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; gbc.anchor = GridBagConstraints.CENTER;
        centerPanel.add(btnStart, gbc);

        JPanel spacer = new JPanel();
        spacer.setPreferredSize(new Dimension(0, 80));
        spacer.setOpaque(false);

        window.add(headerPanel, BorderLayout.NORTH);
        window.add(centerPanel, BorderLayout.CENTER);
        window.add(spacer, BorderLayout.SOUTH);
        window.pack();
    }

    private static void toggleSpinners(boolean enabled, JSpinner... spinners) {
        for (JSpinner s : spinners)
            s.setEnabled(enabled);
    }

    private static void gameStart(int iloscBomb, int iloscKolumn, int iloscWierszy) {
        int maxBombs = (iloscWierszy * iloscKolumn) - 9;
        if(iloscBomb > maxBombs) {
            JOptionPane.showMessageDialog(window, "Zbyt dużo bomb dla tej wielkości planszy! Maksimum to: " + maxBombs, "Błąd", JOptionPane.ERROR_MESSAGE);
            return;
        }

        firstClick = true;
        isGameOver = false;
        gameRows = iloscWierszy;
        gameCols = iloscKolumn;
        gameBombs = iloscBomb;
        flagsCount = 0;
        secondsPassed = 0;
        gameTimer.stop();

        grid = new Field[iloscWierszy][iloscKolumn];
        buttons = new JButton[iloscWierszy][iloscKolumn];

        for (int r = 0; r < iloscWierszy; r++)
            for (int c = 0; c < iloscKolumn; c++)
                grid[r][c] = new Field();

        int cellSize = 32;
        JPanel gridPanel = new JPanel(new GridLayout(iloscWierszy, iloscKolumn, 1, 1));
        gridPanel.setPreferredSize(new Dimension(iloscKolumn * cellSize, iloscWierszy * cellSize));

        for (int r = 0; r < iloscWierszy; r++) {
            for (int c = 0; c < iloscKolumn; c++) {
                JButton btn = new JButton();
                Dimension btnSize = new Dimension(32, 32);
                btn.setPreferredSize(btnSize);
                btn.setMinimumSize(btnSize);
                btn.setMaximumSize(btnSize);
                btn.setMargin(new Insets(0, 0, 0, 0));
                btn.setFont(new Font("SansSerif", Font.BOLD, 14));
                buttons[r][c] = btn;

                final int row = r, col = c;
                btn.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (isGameOver) return;
                        if (SwingUtilities.isLeftMouseButton(e))  odkryj(row, col);
                        else if (SwingUtilities.isRightMouseButton(e)) oznaczFlaga(row, col);
                        else if (SwingUtilities.isMiddleMouseButton(e)) czyszczenieChmury(row, col);
                    }
                });

                gridPanel.add(btn);
            }
        }

        headerPanel.removeAll();
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        lblBombsLeft = new JLabel("🚩 " + gameBombs);
        lblBombsLeft.setFont(new Font("SansSerif", Font.BOLD, 16));

        lblTimer = new JLabel("⏱️ 000");
        lblTimer.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblTimer.setHorizontalAlignment(SwingConstants.RIGHT);

        JButton btnBack = new JButton("Menu");
        btnBack.addActionListener(e -> wrocDoMenu());

        JPanel btnWrapper = new JPanel(new GridBagLayout());
        btnWrapper.add(btnBack);

        headerPanel.add(lblBombsLeft, BorderLayout.WEST);
        headerPanel.add(btnWrapper, BorderLayout.CENTER);
        headerPanel.add(lblTimer, BorderLayout.EAST);
        headerPanel.revalidate();
        headerPanel.repaint();

        centerPanel.setPreferredSize(null);
        centerPanel.removeAll();
        centerPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        centerPanel.setLayout(new GridBagLayout());
        centerPanel.add(gridPanel);
        centerPanel.revalidate();
        centerPanel.repaint();

        window.revalidate();
        window.pack();
        window.setLocationRelativeTo(null);
    }

    private static void rozmiescBomby(int iloscBomb, int excludeR, int excludeC) {
        Random rand = new Random();
        int rozmieszczone = 0;
        while (rozmieszczone < iloscBomb) {
            int r = rand.nextInt(gameRows);
            int c = rand.nextInt(gameCols);

            // true, jeśli wylosowane pole (r, c) znajduje się w obszarze 3x3 wokół pierwszego kliknięcia (excludeR, excludeC),
            // czyli jest zbyt blisko startowego pola i nie może zawierać bomby
            boolean jestWykluczone = Math.abs(r - excludeR) <= 1 && Math.abs(c - excludeC) <= 1;

            if(!grid[r][c].isBomb() && !jestWykluczone) {
                grid[r][c].setBomb(true);
                rozmieszczone++;
            }
        }
    }

    private static void obliczSasiadow() {
        for (int r = 0; r < gameRows; r++) {
            for (int c = 0; c < gameCols; c++) {
                if (grid[r][c].isBomb()) continue;
                int bombCountAround = 0;
                for (int rowOffset = -1; rowOffset <= 1; rowOffset++)
                    for (int colOffset = -1; colOffset <= 1; colOffset++) {
                        /*  wektory przesuniecia
                            [-1,-1]  [-1,0]  [-1,1]
                            [ 0,-1]  [ 0,0]  [ 0,1]
                            [ 1,-1]  [ 1,0]  [ 1,1]
                         */
                        int neighborRow = r + rowOffset;
                        int neighborCol = c + colOffset;
                        if (neighborRow >= 0 && neighborRow < gameRows && neighborCol >= 0 && neighborCol < gameCols) //IM FUCKING TWEAKING (ArrayIndexOutOfBoundsException)
                            if (grid[neighborRow][neighborCol].isBomb()) bombCountAround++;
                    }
                grid[r][c].setAdjacentBombs(bombCountAround);
            }
        }
    }

    private static String toHex(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }

    private static void odkryj(int row, int col) {
        if (isGameOver) return;
        if (row < 0 || row >= gameRows || col < 0 || col >= gameCols) return;
        Field field = grid[row][col];
        if(field.isRevealed() || field.isFlagged()) return;

        if (firstClick) {
            firstClick = false;
            rozmiescBomby(gameBombs, row, col);
            obliczSasiadow();
            gameTimer.start();
        }

        field.setRevealed(true);
        JButton btn = buttons[row][col];
//        btn.setEnabled(false);      - nie, bo wyłączało kolory...
        btn.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Button.borderColor"), 1));
        btn.setBackground(UIManager.getColor("Button.disabledBackground"));

        if(field.isBomb()) {
            btn.setText("\uD83D\uDCA3");
            btn.setForeground(Color.RED);
            gameOver(false);
            return;
        }

        if(field.getAdjacentBombs() > 0) {
            String kolor = toHex(Objects.requireNonNull(KOLORY_CYFR[field.getAdjacentBombs()]));
            btn.setText(String.format("<html><font color='%s'>%d</font></html>", kolor, field.getAdjacentBombs()));
        } else {
            for(int dr = -1; dr <= 1; dr++)
                for (int dc = -1; dc <= 1; dc++)
                    odkryj(row + dr, col + dc);
        }

        sprawdzWygrana(); //wywoluje się tylko wtedy, kiedy nie było bomby
    }

    private static void oznaczFlaga(int row, int col) {
        if (isGameOver) return;
        Field field = grid[row][col];
        if (field.isRevealed()) return;

        field.setFlagged(!field.isFlagged());
        buttons[row][col].setText(field.isFlagged() ? "\uD83D\uDEA9" : "");
        buttons[row][col].setForeground(new Color(200, 30, 30));

        if (field.isFlagged()) flagsCount++;
        else flagsCount--;

        if (lblBombsLeft != null) {
            lblBombsLeft.setText("🚩 " + (gameBombs - flagsCount));
        }
    }

    private static void czyszczenieChmury(int row, int col) {
        Field field = grid[row][col];
        if(!field.isRevealed() || field.getAdjacentBombs() == 0) return;

        int adjacentFlags = 0; //ile flag stoi wokół tego punktu
        for (int rowOffset = -1; rowOffset <= 1; rowOffset++) {
            for (int colOffset = -1; colOffset <= 1; colOffset++) {
                int neighbourRow = row + rowOffset;
                int neighbourCol = col + colOffset;

                if(neighbourRow >= 0 && neighbourRow < gameRows && neighbourCol >= 0 && neighbourCol < gameCols) {
                    if(grid[neighbourRow][neighbourCol].isFlagged()) {
                        adjacentFlags++;
                    }
                }
            }
        }

        // jeśli postawiona jest odpowiednia liczba flag w poprawnych miejscach - odkrywają się wszystkie pola wokół
        if(adjacentFlags == field.getAdjacentBombs()) {
            for (int rowOffset = -1; rowOffset <= 1; rowOffset++) {
                for (int colOffset = -1; colOffset <= 1; colOffset++) {
                    int neighbourRow = row + rowOffset;
                    int neighbourCol = col + colOffset;

                    if(neighbourRow >= 0 && neighbourRow < gameRows && neighbourCol >= 0 && neighbourCol < gameCols) {
                        Field neighbour = grid[neighbourRow][neighbourCol];
                        if(!neighbour.isRevealed() && !neighbour.isFlagged()) {
                            odkryj(neighbourRow, neighbourCol);
                        }
                    }
                }
            }
        }
    }

    private static void sprawdzWygrana() {
        if (isGameOver) return;
        for (int r = 0; r < gameRows; r++)
            for (int c = 0; c < gameCols; c++)
                if (!grid[r][c].isBomb() && !grid[r][c].isRevealed())
                    return; // jeszcze nie wszystko odkryte
        gameOver(true);
    }

    private static void wrocDoMenu() {
        gameTimer.stop();
        window.getContentPane().removeAll();
        setupUI();
        window.revalidate();
        window.pack();
        window.setLocationRelativeTo(null);
    }

    private static void gameOver(boolean wygrana) {
        if (isGameOver) return;
        isGameOver = true;
        gameTimer.stop();

        for (int r = 0; r < gameRows; r++) {
            for (int c = 0; c < gameCols; c++) {
                buttons[r][c].setEnabled(false);
                if (grid[r][c].isBomb()) {
                    if(wygrana) {
                        buttons[r][c].setText("\uD83D\uDEA9");
                        buttons[r][c].setForeground(new Color(200, 30, 30));
                    } else {
                        buttons[r][c].setText("\uD83D\uDCA3");
                        buttons[r][c].setForeground(new Color(200, 30, 30));
                    }
                }
            }
        }

        String komunikat = "";
        if(wygrana) {
            komunikat = "Wygrałeś! Gratulacje! 🎉\nTwój czas: " + secondsPassed + " sekund.";

            if (!currentPreset.equals("Custom")) {
                int obecnyRekord = prefs.getInt("highscore_" + currentPreset, 9999);
                if (secondsPassed < obecnyRekord) {
                    prefs.putInt("highscore_" + currentPreset, secondsPassed);
                    komunikat += "\n✨ NOWY REKORD TEGO POZIOMU! ✨";
                }
            }
        } else {
            komunikat = "Bomba! 💥 Przegrałeś :(";
        }
        
        int wybor = JOptionPane.showConfirmDialog(window, komunikat + "\nWrócić do menu?", "Koniec gry", JOptionPane.YES_NO_OPTION);
        if(wybor == JOptionPane.YES_OPTION) wrocDoMenu();
    }

    private static void finalizeWindowSetup() {
        window.setMinimumSize(new Dimension(450, 550));
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.setResizable(true);
    }

    public static void applyTheme(boolean isDark) {
        if (isDark) {
            FlatDarkLaf.setup();
        } else {
            FlatLightLaf.setup();
        }
    }

    private static boolean isDarkFromPrefs() {
        String theme = prefs.get("theme", "system");
        if (theme.equals("dark")) return true;
        if (theme.equals("light")) return false;
        return OsThemeDetector.getDetector().isDark();
    }

    public static void main(String[] args) {
        prefs = Preferences.userNodeForPackage(Main.class);
        applyTheme(isDarkFromPrefs());

        SwingUtilities.invokeLater(Main::makeWindow);
    }
}
