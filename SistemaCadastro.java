package com.mycompany.sistemacadastro;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.math.BigDecimal;
import java.net.URL;
import javax.imageio.ImageIO;
import java.sql.*;
import java.util.Vector;

public class SistemaCadastro extends JFrame {

    private JTable resultadosTable;
    private DefaultTableModel tableModel;
    private JTextField cpfCnpjField;

    public SistemaCadastro() {
        setTitle("Dashboard - Sistema de Cadastro");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setIconImage(Toolkit.getDefaultToolkit().getImage("icon.png"));

        initUI();
    }

    private void initUI() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        mainPanel.setBackground(new Color(240, 240, 240));

      
        JLabel logoLabel = new JLabel();
        try {
            BufferedImage logoImage = ImageIO.read(new URL("https://i.imgur.com/LG9X0st.png"));
            Image scaledImage = logoImage.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            logoLabel.setIcon(new ImageIcon(scaledImage));
        } catch (IOException e) {
            showError("Erro ao carregar a logo.");
        }
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);
        mainPanel.add(logoLabel, gbc);

        // Painel de busca
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(new Color(240, 240, 240));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Busca de Cliente"));
        searchPanel.setPreferredSize(new Dimension(800, 80));
        searchPanel.add(new JLabel("CPF ou CNPJ:"));

        cpfCnpjField = new JTextField(15);
        searchPanel.add(cpfCnpjField);

        JButton buscarButton = new JButton("Buscar");
        searchPanel.add(buscarButton);

        JButton verTodosButton = new JButton("Ver Todos");
        searchPanel.add(verTodosButton);

        JButton cadastrarButton = new JButton("Cadastrar Cliente");
        searchPanel.add(cadastrarButton);

        JButton alterarButton = new JButton("Alterar Cliente");
        searchPanel.add(alterarButton);

        JButton excluirButton = new JButton("Excluir Cliente");
        searchPanel.add(excluirButton);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(searchPanel, gbc);

        // Tabela para resultados
        String[] columnNames = {"NOME", "CPF/CNPJ", "E-MAIL", "TELEFONE", "CELULAR", "VALOR MENSALIDADE", "DIA VENCIMENTO", "RUA", "Nº", "BAIRRO", "CEP"};
        tableModel = new DefaultTableModel(columnNames, 0);
        resultadosTable = new JTable(tableModel);
        resultadosTable.setFillsViewportHeight(true);
        resultadosTable.setFont(new Font("Arial", Font.PLAIN, 14));
        resultadosTable.setRowHeight(30);
        resultadosTable.setBackground(Color.WHITE);
        resultadosTable.setForeground(new Color(50, 50, 50));
        resultadosTable.setGridColor(new Color(200, 200, 200));
        resultadosTable.setSelectionBackground(new Color(173, 216, 230));
        resultadosTable.setSelectionForeground(Color.BLACK);

        JScrollPane scrollPane = new JScrollPane(resultadosTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("RESULTADOS DA BUSCA"));
        scrollPane.setPreferredSize(new Dimension(900, 300));

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1;
        mainPanel.add(scrollPane, gbc);

        // Estilizando botões
        JButton[] buttons = {buscarButton, verTodosButton, cadastrarButton, alterarButton, excluirButton};
        for (JButton button : buttons) {
            button.setBackground(new Color(0, 122, 204));
            button.setForeground(Color.WHITE);
            button.setFont(new Font("Arial", Font.BOLD, 12));
            button.setFocusPainted(false);
            button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        }

        add(mainPanel);

        buscarButton.addActionListener(e -> buscarCliente());
        verTodosButton.addActionListener(e -> mostrarTodosClientes());
        cadastrarButton.addActionListener(e -> cadastrarCliente());
        alterarButton.addActionListener(e -> alterarCliente());
        excluirButton.addActionListener(e -> excluirCliente());
    }

    private void buscarCliente() {
        String cpfCnpj = cpfCnpjField.getText().trim();

        if (cpfCnpj.isEmpty()) {
            showWarning("Por favor, insira um CPF ou CNPJ.");
            return;
        }

        if (!isValidCpfCnpj(cpfCnpj)) {
            showWarning("CPF ou CNPJ inválido.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT c.*, e.rua, e.numero, e.bairro, e.cep FROM clientes c LEFT JOIN endereco e ON c.id = e.cliente_id WHERE c.cpf_cnpj = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, cpfCnpj);
                ResultSet rs = stmt.executeQuery();

                tableModel.setRowCount(0);

                if (rs.next()) {
                    Vector<String> row = new Vector<>();
                    row.add(rs.getString("nome").toUpperCase());
                    row.add(rs.getString("cpf_cnpj").toUpperCase());
                    row.add(rs.getString("email").toUpperCase());
                    row.add(rs.getString("telefone").toUpperCase());
                    row.add(rs.getString("celular").toUpperCase());
                    row.add("R$ " + rs.getBigDecimal("valor_mensalidade").toString());
                    row.add("Dia " + rs.getInt("dia_vencimento"));
                    row.add(rs.getString("rua").toUpperCase());
                    row.add(rs.getString("numero").toUpperCase());
                    row.add(rs.getString("bairro").toUpperCase());
                    row.add(rs.getString("cep").toUpperCase());
                    tableModel.addRow(row);
                } else {
                    showInformation("Cliente não encontrado.");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            showError("Erro ao buscar cliente.");
        }
    }

    private boolean isValidCpfCnpj(String cpfCnpj) {
        return cpfCnpj.matches("\\d{11}") || cpfCnpj.matches("\\d{14}");
    }

    private void mostrarTodosClientes() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT c.*, e.rua, e.numero, e.bairro, e.cep FROM clientes c LEFT JOIN endereco e ON c.id = e.cliente_id";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();

                tableModel.setRowCount(0);

                while (rs.next()) {
                    Vector<String> row = new Vector<>();
                    row.add(rs.getString("nome").toUpperCase());
                    row.add(rs.getString("cpf_cnpj").toUpperCase());
                    row.add(rs.getString("email").toUpperCase());
                    row.add(rs.getString("telefone").toUpperCase());
                    row.add(rs.getString("celular").toUpperCase());
                    row.add("R$ " + rs.getBigDecimal("valor_mensalidade").toString());
                    row.add("Dia " + rs.getInt("dia_vencimento"));
                    row.add(rs.getString("rua").toUpperCase());
                    row.add(rs.getString("numero").toUpperCase());
                    row.add(rs.getString("bairro").toUpperCase());
                    row.add(rs.getString("cep").toUpperCase());
                    tableModel.addRow(row);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            showError("Erro ao mostrar todos os clientes.");
        }
    }

    private void cadastrarCliente() {
        // Implementação da funcionalidade de cadastro
        JPanel panel = new JPanel(new GridLayout(0, 2));
        JTextField nomeField = new JTextField();
        JTextField cpfCnpjField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField telefoneField = new JTextField();
        JTextField celularField = new JTextField();
        JTextField valorMensalidadeField = new JTextField();
        JTextField diaVencimentoField = new JTextField();
        JTextField ruaField = new JTextField();
        JTextField numeroField = new JTextField();
        JTextField bairroField = new JTextField();
        JTextField cepField = new JTextField();

        panel.add(new JLabel("Nome:"));
        panel.add(nomeField);
        panel.add(new JLabel("CPF ou CNPJ:"));
        panel.add(cpfCnpjField);
        panel.add(new JLabel("E-mail:"));
        panel.add(emailField);
        panel.add(new JLabel("Telefone:"));
        panel.add(telefoneField);
        panel.add(new JLabel("Celular:"));
        panel.add(celularField);
        panel.add(new JLabel("Valor Mensalidade:"));
        panel.add(valorMensalidadeField);
        panel.add(new JLabel("Dia Vencimento:"));
        panel.add(diaVencimentoField);
        panel.add(new JLabel("Rua:"));
        panel.add(ruaField);
        panel.add(new JLabel("Número:"));
        panel.add(numeroField);
        panel.add(new JLabel("Bairro:"));
        panel.add(bairroField);
        panel.add(new JLabel("CEP:"));
        panel.add(cepField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Cadastrar Cliente", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String cpfCnpj = cpfCnpjField.getText().trim().replaceAll("[^\\d]", ""); 
            if (!isValidCpfCnpj(cpfCnpj)) {
                showWarning("CPF ou CNPJ inválido. Por favor, preencha sem pontos ou traços.");
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "INSERT INTO clientes (nome, cpf_cnpj, email, telefone, celular, valor_mensalidade, dia_vencimento) VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, nomeField.getText().trim());
                    stmt.setString(2, cpfCnpj);
                    stmt.setString(3, emailField.getText().trim());
                    stmt.setString(4, telefoneField.getText().trim());
                    stmt.setString(5, celularField.getText().trim());
                    stmt.setBigDecimal(6, new BigDecimal(valorMensalidadeField.getText().trim()));
                    stmt.setInt(7, Integer.parseInt(diaVencimentoField.getText().trim()));
                    stmt.executeUpdate();
                    
                    
                    String sqlEndereco = "INSERT INTO endereco (cliente_id, rua, numero, bairro, cep) VALUES ((SELECT id FROM clientes WHERE cpf_cnpj = ?), ?, ?, ?, ?)";
                    try (PreparedStatement stmtEndereco = conn.prepareStatement(sqlEndereco)) {
                        stmtEndereco.setString(1, cpfCnpj);
                        stmtEndereco.setString(2, ruaField.getText().trim());
                        stmtEndereco.setString(3, numeroField.getText().trim());
                        stmtEndereco.setString(4, bairroField.getText().trim());
                        stmtEndereco.setString(5, cepField.getText().trim());
                        stmtEndereco.executeUpdate();
                    }

                    showInformation("Cliente cadastrado com sucesso!");
                    mostrarTodosClientes();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                showError("Erro ao cadastrar cliente.");
            }
        }
    }

    private void alterarCliente() {
        int selectedRow = resultadosTable.getSelectedRow();
        if (selectedRow == -1) {
            showWarning("Selecione um cliente para alterar.");
            return;
        }

        String cpfCnpjAtual = tableModel.getValueAt(selectedRow, 1).toString(); // CPF/CNPJ atual

        JPanel panel = new JPanel(new GridLayout(0, 2));
        panel.add(new JLabel("Nome:"));
        JTextField nomeField = new JTextField(tableModel.getValueAt(selectedRow, 0).toString());
        panel.add(nomeField);
        panel.add(new JLabel("CPF ou CNPJ:"));
        JTextField cpfCnpjField = new JTextField(cpfCnpjAtual);
        panel.add(cpfCnpjField);
        panel.add(new JLabel("E-mail:"));
        JTextField emailField = new JTextField(tableModel.getValueAt(selectedRow, 2).toString());
        panel.add(emailField);
        panel.add(new JLabel("Telefone:"));
        JTextField telefoneField = new JTextField(tableModel.getValueAt(selectedRow, 3).toString());
        panel.add(telefoneField);
        panel.add(new JLabel("Celular:"));
        JTextField celularField = new JTextField(tableModel.getValueAt(selectedRow, 4).toString());
        panel.add(celularField);
        panel.add(new JLabel("Valor Mensalidade:"));
        JTextField valorMensalidadeField = new JTextField(tableModel.getValueAt(selectedRow, 5).toString().replace("R$ ", ""));
        panel.add(valorMensalidadeField);
        panel.add(new JLabel("Dia Vencimento:"));
        JTextField diaVencimentoField = new JTextField(tableModel.getValueAt(selectedRow, 6).toString().replace("Dia ", ""));
        panel.add(diaVencimentoField);
        panel.add(new JLabel("Rua:"));
        JTextField ruaField = new JTextField(tableModel.getValueAt(selectedRow, 7).toString());
        panel.add(ruaField);
        panel.add(new JLabel("Número:"));
        JTextField numeroField = new JTextField(tableModel.getValueAt(selectedRow, 8).toString());
        panel.add(numeroField);
        panel.add(new JLabel("Bairro:"));
        JTextField bairroField = new JTextField(tableModel.getValueAt(selectedRow, 9).toString());
        panel.add(bairroField);
        panel.add(new JLabel("CEP:"));
        JTextField cepField = new JTextField(tableModel.getValueAt(selectedRow, 10).toString());
        panel.add(cepField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Alterar Cliente", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String cpfCnpj = cpfCnpjField.getText().trim().replaceAll("[^\\d]", ""); 
            if (!isValidCpfCnpj(cpfCnpj)) {
                showWarning("CPF ou CNPJ inválido. Por favor, preencha sem pontos ou traços.");
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "UPDATE clientes SET nome = ?, cpf_cnpj = ?, email = ?, telefone = ?, celular = ?, valor_mensalidade = ?, dia_vencimento = ? WHERE cpf_cnpj = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, nomeField.getText().trim());
                    stmt.setString(2, cpfCnpj);
                    stmt.setString(3, emailField.getText().trim());
                    stmt.setString(4, telefoneField.getText().trim());
                    stmt.setString(5, celularField.getText().trim());
                    stmt.setBigDecimal(6, new BigDecimal(valorMensalidadeField.getText().trim()));
                    stmt.setInt(7, Integer.parseInt(diaVencimentoField.getText().trim()));
                    stmt.setString(8, cpfCnpjAtual); // CPF/CNPJ atual
                    stmt.executeUpdate();

                    // Atualizar endereço se necessário
                    String sqlEndereco = "UPDATE endereco SET rua = ?, numero = ?, bairro = ?, cep = ? WHERE cliente_id = (SELECT id FROM clientes WHERE cpf_cnpj = ?)";
                    try (PreparedStatement stmtEndereco = conn.prepareStatement(sqlEndereco)) {
                        stmtEndereco.setString(1, ruaField.getText().trim());
                        stmtEndereco.setString(2, numeroField.getText().trim());
                        stmtEndereco.setString(3, bairroField.getText().trim());
                        stmtEndereco.setString(4, cepField.getText().trim());
                        stmtEndereco.setString(5, cpfCnpjAtual); // CPF/CNPJ atual
                        stmtEndereco.executeUpdate();
                    }

                    showInformation("Cliente alterado com sucesso!");
                    mostrarTodosClientes();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                showError("Erro ao alterar cliente.");
            }
        }
    }

    private void excluirCliente() {
        int selectedRow = resultadosTable.getSelectedRow();
        if (selectedRow == -1) {
            showWarning("Selecione um cliente para excluir.");
            return;
        }

        String cpfCnpj = tableModel.getValueAt(selectedRow, 1).toString(); // CPF/CNPJ do cliente selecionado

        int confirm = JOptionPane.showConfirmDialog(this, "Tem certeza que deseja excluir o cliente " + cpfCnpj + "?", "Confirmar Exclusão", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "DELETE FROM clientes WHERE cpf_cnpj = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, cpfCnpj);
                    stmt.executeUpdate();
                    showInformation("Cliente excluído com sucesso!");
                    mostrarTodosClientes();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                showError("Erro ao excluir cliente.");
            }
        }
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Atenção", JOptionPane.WARNING_MESSAGE);
    }

    private void showInformation(String message) {
        JOptionPane.showMessageDialog(this, message, "Informação", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SistemaCadastro sistema = new SistemaCadastro();
            sistema.setVisible(true);
        });
    }
}
