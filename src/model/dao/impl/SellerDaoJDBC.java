package model.dao.impl;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import db.DB;
import db.DbException;
import model.dao.SellerDao;
import model.entitites.Department;
import model.entitites.Seller;

public class SellerDaoJDBC implements SellerDao {

	private Connection conn;

	public SellerDaoJDBC(Connection conn) {
		this.conn = conn;
	}

	@Override
	public void insert(Seller seller) {

		PreparedStatement st = null;

		try {

			StringBuilder query = new StringBuilder();
			query.append("INSERT INTO seller ");
			query.append("(Name, Email, BirthDate, BaseSalary, DepartmentId) ");
			query.append("VALUES ");
			query.append("(?, ?, ?, ?, ?)");

			st = conn.prepareStatement(query.toString(), Statement.RETURN_GENERATED_KEYS);

			st.setString(1, seller.getName());
			st.setString(2, seller.getEmail());
			st.setDate(3, new Date(seller.getBirthDate().getTime()));
			st.setDouble(4, seller.getBaseSalary());
			st.setInt(5, seller.getDepartment().getId());

			int rowsAffected = st.executeUpdate();

			if (rowsAffected > 0) {
				ResultSet rs = st.getGeneratedKeys();
				if (rs.next()) {
					int id = rs.getInt(1);
					seller.setId(id);
				}
				DB.closeResultSet(rs);
			} else {
				throw new DbException("Erro inesperado, nenhuma linha foi afetada");
			}
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		} finally {
			DB.closeStatement(st);
		}
	}

	@Override
	public void update(Seller seller) {
		PreparedStatement st = null;
		try {
			StringBuilder query = new StringBuilder();
			query.append("UPDATE seller SET ");
			query.append("Name = ?, Email = ?, BirthDate = ?, BaseSalary = ?, DepartmentId = ? ");
			query.append("WHERE Id = ?");

			st = conn.prepareStatement(query.toString());

			st.setString(1, seller.getName());
			st.setString(2, seller.getEmail());
			st.setDate(3, new Date(seller.getBirthDate().getTime()));
			st.setDouble(4, seller.getBaseSalary());
			st.setInt(5, seller.getDepartment().getId());
			st.setInt(6, seller.getId());

			st.executeUpdate();

		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		} finally {
			DB.closeStatement(st);

		}

	}

	@Override
	public void deleteById(Integer id) {

		PreparedStatement st = null;
		StringBuilder query = new StringBuilder();

		try {

			query.append("DELETE FROM seller ");
			query.append("WHERE Id=?");

			st = conn.prepareStatement(query.toString());
			st.setInt(1, id);
			int rowsAffected = st.executeUpdate();

			if (rowsAffected == 0) {
				throw new DbException("Não foi encontrado o id");
			}

		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		} finally {
			DB.closeStatement(st);
		}
	}

	/**
	 * Retorna o Vendendor por Id
	 * 
	 * @return Seller
	 * 
	 * @author Fernando
	 */
	@Override
	public Seller findById(Integer id) {
		PreparedStatement st = null;
		ResultSet rs = null;

		try {
			st = conn.prepareStatement(
					"SELECT seller.*,department.Name as DepName" + " From seller INNER JOIN department"
							+ " ON seller.DepartmentId = department.Id" + " Where seller.Id = ?");
			st.setInt(1, id);
			rs = st.executeQuery();// recebe o resultado da operação executeQuery()

			// O rs aponta para pos. 0 ( null), só na pos. 1 que é armazenado os dados
			// É chamado a operação next para verificar se há dados na posição 1.
			// Se minha consulta não retornou nenhum registro, meu rs vai dar falso, pular o
			// if
			// e vai retorna null
			if (rs.next()) {
				Department dep = instantiateDepartment(rs);
				Seller seller = instantiateSeller(rs, dep);
				return seller;
			}
			return null;
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		} finally {
			DB.closeResultSet(rs);
			DB.closeStatement(st);
		}
	}

	@Override
	public List<Seller> finbByDepartment(Department department) {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement(
					"SELECT seller.*,department.Name as DepName " + "FROM seller INNER JOIN department "
							+ "ON seller.DepartmentId = department.Id " + "WHERE DepartmentId = ? " + "Order BY Name");
			st.setInt(1, department.getId());
			rs = st.executeQuery();

			List<Seller> seller = new ArrayList<>();
			Map<Integer, Department> map = new HashMap<>(); // Foi criado um map vazio, onde será guardado qlq
															// departamento instaciado

			while (rs.next()) {

				Department dep = map.get(rs.getInt("DepartmentId")); // busca um departamento com o DepartmentId

				if (dep == null) {
					dep = instantiateDepartment(rs);
					map.put(rs.getInt("DepartmentId"), dep);// guarda a chava e o dep, na proxima repeticao do while, se
															// já existir o dep dentro do map , não será instanciado
				}

				Seller sellerList = instantiateSeller(rs, dep);
				seller.add(sellerList);
			}
			return seller;
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}

	private Seller instantiateSeller(ResultSet rs, Department dep) throws SQLException {
		Seller seller = new Seller();
		seller.setId(rs.getInt("Id"));
		seller.setName(rs.getString("Name"));
		seller.setEmail(rs.getString("Email"));
		seller.setBaseSalary(rs.getDouble("BaseSalary"));
		seller.setBirthDate(rs.getDate("BirthDate"));
		seller.setDepartment(dep);
		return seller;
	}

	private Department instantiateDepartment(ResultSet rs) throws SQLException {

		Department dep = new Department();
		dep.setId(rs.getInt("DepartmentId"));
		dep.setName(rs.getString("DepName"));
		return dep;
	}

	@Override
	public List<Seller> findAll() {
		PreparedStatement st = null;
		ResultSet rs = null;

		try {
			st = conn.prepareStatement(
					"SELECT seller.*,department.Name as DepName " + "FROM seller INNER JOIN department "
							+ "ON seller.DepartmentId = department.Id " + "ORDER BY Name");

			rs = st.executeQuery();

			List<Seller> sellerList = new ArrayList<>();
			Map<Integer, Department> map = new HashMap<>();

			while (rs.next()) {

				Department dep = map.get(rs.getInt("DepartmentId"));

				if (dep == null) {
					dep = instantiateDepartment(rs);
					map.put(rs.getInt("DepartmentId"), dep);
				}

				Seller seller = instantiateSeller(rs, dep);

				sellerList.add(seller);
			}
			return sellerList;
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}
}
