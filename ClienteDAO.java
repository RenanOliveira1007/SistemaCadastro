package com.mycompany.sistemacadastro;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ClienteDAO {
    
    public void cadastrarCliente(Cliente cliente) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO clientes (nome, cpf_cnpj, email, telefone, celular, valor_mensalidade, dia_vencimento) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, cliente.getNome());
                stmt.setString(2, cliente.getCpfCnpj());
                stmt.setString(3, cliente.getEmail());
                stmt.setString(4, cliente.getTelefone());
                stmt.setString(5, cliente.getCelular());
                stmt.setBigDecimal(6, cliente.getValorMensalidade());
                stmt.setInt(7, cliente.getDiaVencimento());
                stmt.executeUpdate();

                
                cadastrarEndereco(conn, cliente.getCpfCnpj(), cliente.getEndereco());
            }
        }
    }

    
    private void cadastrarEndereco(Connection conn, String cpfCnpj, Endereco endereco) throws SQLException {
        String sqlEndereco = "INSERT INTO endereco (cliente_id, rua, numero, bairro, cep) VALUES ((SELECT id FROM clientes WHERE cpf_cnpj = ?), ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sqlEndereco)) {
            stmt.setString(1, cpfCnpj);
            stmt.setString(2, endereco.getRua());
            stmt.setString(3, endereco.getNumero());
            stmt.setString(4, endereco.getBairro());
            stmt.setString(5, endereco.getCep());
            stmt.executeUpdate();
        }
    }

}
