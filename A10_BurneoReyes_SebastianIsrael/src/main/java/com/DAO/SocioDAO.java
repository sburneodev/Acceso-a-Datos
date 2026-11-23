package com.DAO;

import com.Conexiones;
import com.Socio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SocioDAO {
    public static int altaSocio(Socio socio) throws SQLException {
        String sql="INSERT INTO socios(nombre,telefono,fecha_alta) VALUES (?,?,?)";
        try(Connection con = Conexiones.getConexion();
            PreparedStatement ps = con.prepareStatement(sql,PreparedStatement.RETURN_GENERATED_KEYS);){
            ps.setString(1, socio.getNombre());
            ps.setString(2, socio.getTelefono());
            ps.setDate(3,java.sql.Date.valueOf(socio.getFechaAlta()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()){
                if (rs.next()) {
                    return rs.getInt(1);
                }
                throw new SQLException("No se dio de alta ningun socio");
            }
        }


    }
}
