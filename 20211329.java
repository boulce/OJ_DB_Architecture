import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class JDBC_Example {
	static final String DB_URL = "jdbc:mysql://localhost/problem_judge?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone= UTC";
	static final String USER = "root"; // user name
	static final String PASS = "0905"; // user password
	static String QUERY;
	
	public static void main(String[] args) {
		int inputNum;
		String inputStr;
		Scanner sc = new Scanner(System.in);
		while(true) {
			System.out.println("-------------기능-------------");
			System.out.println("1.회원 정보 조회");
			System.out.println("\n2.문제 정보 조회");
			
			System.out.println("\n3. 정보 초기화");
			System.out.println("0.종료");
			System.out.println("-----------------------------");
			System.out.print("번호를 입력해주세요: ");
			inputNum = sc.nextInt();
			
			
			if(inputNum == 1) {			
				Connection conn = null;
				Statement stmt = null;
				ResultSet rs = null;
				PreparedStatement pstmt = null;
				
				System.out.print("조회하려는 회원 ID를 입력해주세요: ");
				inputStr = sc.next();
				
				try {
					conn = DriverManager.getConnection(DB_URL, USER, PASS);
					stmt = conn.createStatement();
					
					QUERY = "select id, password, name, (total_exp DIV 100 + 1) as level, total_exp, title\n"
							+ "from member";
					rs = stmt.executeQuery(QUERY);
					System.out.println("\n<1번 기능을 실행하기 전 member 테이블>");
					while(rs.next()) {
						System.out.printf("아이디: %8s | 비밀번호: %14s | 이름: %8s | 레벨: %3d | 누적경험치: %5.2f | 권한: %7s\n"
								,rs.getString("id"), rs.getString("password"), rs.getString("name"), rs.getInt("level"), rs.getFloat("total_exp"), rs.getString("title"));
					}
				} catch(SQLException e) {
					System.out.println("SQLException : " + e);
				} finally {
					try {
						rs.close();
						stmt.close();
						conn.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
				
				try {
					conn = DriverManager.getConnection(DB_URL, USER, PASS);
					stmt = conn.createStatement();
					
					QUERY = "update member as T\n"
							+ "set total_exp =  (select case\n"
							+ "										when SUM(difficulty) is not null then SUM(difficulty) \n"
							+ "										else 0							\n"
							+ "									end\n"
							+ "						   from problem as K\n"
							+ "						   where K.difficulty is not null \n"
							+ "									  and exists (select *\n"
							+ "											    		    from submission as S\n"
							+ "													    where T.id = S.id \n"
							+ "																   and K.problem_id = S.problem_id\n"
							+ "																   and S.is_correct = 1))";
					stmt.executeUpdate(QUERY);
					QUERY = "update member as T\n"
							+ "set total_exp =   total_exp + \n"
							+ "						  (select COUNT(*) * 100\n"
							+ "						   from problem as K\n"
							+ "						   where K.difficulty is null\n"
							+ "									  and exists(select *\n"
							+ "													  from submission as S\n"
							+ "													  where T.id = S.id \n"
							+ "													  			 and K.problem_id = S.problem_id\n"
							+ "													  			 and S.is_correct = 1))";
					stmt.executeUpdate(QUERY);
					
					QUERY = "select id, password, name, (total_exp DIV 100 + 1) as level, total_exp, title\n"
							+ "from member";
					rs = stmt.executeQuery(QUERY);
					System.out.println("\n<1번 기능을 실행한 후 member 테이블>");
					while(rs.next()) {
						System.out.printf("아이디: %8s | 비밀번호: %14s | 이름: %8s | 레벨: %3d | 누적경험치: %5.2f | 권한: %7s\n"
								,rs.getString("id"), rs.getString("password"), rs.getString("name"), rs.getInt("level"), rs.getFloat("total_exp"), rs.getString("title"));
					}
					System.out.println("");
				} catch(SQLException e) {
					System.out.println("SQLException : " + e);
				} finally {
					try {
						rs.close();
						stmt.close();
						conn.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
				
				try {
					conn = DriverManager.getConnection(DB_URL, USER, PASS);
					
					QUERY = "select id, name, (total_exp DIV 100 + 1) as level, total_exp, title\n"
							+ "from member\n"
							+ "where id = ?";
					pstmt = conn.prepareStatement(QUERY);
					pstmt.setString(1, inputStr);
					rs = pstmt.executeQuery();
					System.out.println("\n<회원 정보 조회 결과>");
					while(rs.next()) {
						System.out.printf("아이디: %8s | 이름: %8s | 레벨: %3d | 누적경험치: %5.2f | 권한: %7s\n"
								,rs.getString("id"), rs.getString("name"), rs.getInt("level"), rs.getFloat("total_exp"), rs.getString("title"));
					}
					System.out.println("");
				} catch(SQLException e) {
					System.out.println("SQLException : " + e);
				} finally {
					try {
						rs.close();
						pstmt.close();
						conn.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
			else if(inputNum == 2) {
				System.out.println("-----------------------------");
				System.out.println("1.고유 번호로 조회");
				System.out.println("\n2.제목으로 조회");
				System.out.println("-----------------------------");
				System.out.print("번호를 입력해주세요: ");
				inputNum = sc.nextInt();
				
				if(inputNum == 1) {
					System.out.print("고유번호를 입력해주세요: ");
				}
				else if(inputNum == 2) {
					System.out.print("제목 입력해주세요: ");
				}
				inputStr = sc.next();
				
				Connection conn = null;
				Statement stmt = null;
				ResultSet rs = null;
				PreparedStatement pstmt = null;
				
				try {
					conn = DriverManager.getConnection(DB_URL, USER, PASS);
					stmt = conn.createStatement();
					
					QUERY = "select *, (case when num_submission <> 0 then \n"
							+ "num_correct / num_submission * 100 else 0 end) as rate_correct\n"
							+ "from problem";
					rs = stmt.executeQuery(QUERY);
					
					System.out.println("\n<2번 기능을 실행하기 전 problem 테이블>");
					while(rs.next()) {
						System.out.printf("고유번호: %4d | 제목: %8s | 설명: %20s | 문제_난이도: %5.2f | 제출한_코드의_수: %4d | 정답_코드의_수: %4d | 정답률: %5.2f | 출처이름: %10s | 출제자아이디 : %10s\n"
								,rs.getInt("problem_id"), rs.getString("title"), rs.getString("description"), rs.getFloat("difficulty"), 
								rs.getInt("num_submission"), rs.getInt("num_correct"), rs.getFloat("rate_correct"), rs.getString("source_name"), rs.getString("examiner_id"));
					}
				} catch(SQLException e) {
					System.out.println("SQLException : " + e);
				} finally {
					try {
						rs.close();
						stmt.close();
						conn.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
				
				try {
					conn = DriverManager.getConnection(DB_URL, USER, PASS);
					stmt = conn.createStatement();
					
					QUERY = "update problem as T\n"
							+ "set difficulty = (select AVG(opinion_value)\n"
							+ "							from difficulty_opinion as S, member as K\n"
							+ "							where  S.id = K.id and K.total_exp DIV 100 + 1 >= 3\n"
							+ "										 and exists (select *\n"
							+ "															 from submission as M\n"
							+ "															 where S.problem_id = M.problem_id \n"
							+ "																		 and S.id = M.id)\n"
							+ "							group by S.problem_id\n"
							+ "							having T.problem_id  = S.problem_id)";
					stmt.executeUpdate(QUERY);
					QUERY = "update problem as T\n"
							+ "set num_submission = (select COUNT(*)\n"
							+ "													 from submission as S\n"
							+ "													 where T.problem_id = S.problem_id)";
					stmt.executeUpdate(QUERY);
					QUERY = "update problem as T\n"
							+ "set num_correct = (select COUNT(*)\n"
							+ "										   from submission as S\n"
							+ "											where T.problem_id = S.problem_id and is_correct = 1)";
					stmt.executeUpdate(QUERY);
					
					QUERY = "select *, (case when num_submission <> 0 then \n"
							+ "num_correct / num_submission * 100 else 0 end) as rate_correct\n"
							+ "from problem";
					rs = stmt.executeQuery(QUERY);
					System.out.println("\n<2번 기능을 실행한 후 problem 테이블>");
					while(rs.next()) {
						System.out.printf("고유번호: %4d | 제목: %8s | 설명: %20s | 문제_난이도: %5.2f | 제출한_코드의_수: %4d | 정답_코드의_수: %4d | 정답률: %5.2f | 출처이름: %10s | 출제자아이디 : %10s\n"
								,rs.getInt("problem_id"), rs.getString("title"), rs.getString("description"), rs.getFloat("difficulty"), 
								rs.getInt("num_submission"), rs.getInt("num_correct"), rs.getFloat("rate_correct"), rs.getString("source_name"), rs.getString("examiner_id"));
					}
					System.out.println("");
				} catch(SQLException e) {
					System.out.println("SQLException : " + e);
				} finally {
					try {
						rs.close();
						stmt.close();
						conn.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
				
				try {
					conn = DriverManager.getConnection(DB_URL, USER, PASS);
					
					if(inputNum == 1) {
						QUERY = "select *, (case when num_submission <> 0 then \n"
								+ "num_correct / num_submission * 100 else 0 end) as rate_correct\n"
								+ "from problem\n"
								+ "where problem_id = ?";
						pstmt = conn.prepareStatement(QUERY);
						pstmt.setInt(1, Integer.parseInt(inputStr));
					}
					else if(inputNum == 2) {
						QUERY = "select *, (case when num_submission <> 0 then \n"
								+ "num_correct / num_submission * 100 else 0 end) as rate_correct\n"
								+ "from problem\n"
								+ "where title = ?";
						pstmt = conn.prepareStatement(QUERY);
						pstmt.setString(1, inputStr);
					}
					rs = pstmt.executeQuery();
					
					System.out.println("\n<문제 정보 조회 결과>");
					while(rs.next()) {
						System.out.printf("고유번호: %4d | 제목: %8s | 설명: %20s | 문제_난이도: %5.2f | 제출한_코드의_수: %4d | 정답_코드의_수: %4d | 정답률: %5.2f | 출처이름: %10s | 출제자아이디 : %10s\n"
								,rs.getInt("problem_id"), rs.getString("title"), rs.getString("description"), rs.getFloat("difficulty"), 
								rs.getInt("num_submission"), rs.getInt("num_correct"), rs.getFloat("rate_correct"), rs.getString("source_name"), rs.getString("examiner_id"));
					}
					System.out.println("");
				} catch(SQLException e) {
					System.out.println("SQLException : " + e);
				} finally {
					try {
						rs.close();
						pstmt.close();
						conn.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
				
			}
			else if(inputNum == 3) {
				Connection conn = null;
				Statement stmt = null;
				
				try {
					conn = DriverManager.getConnection(DB_URL, USER, PASS);
					stmt = conn.createStatement();
					
					QUERY = "update member set total_exp = 0";
					stmt.executeUpdate(QUERY);
					
					QUERY = "update problem set difficulty = null";
					stmt.executeUpdate(QUERY);
					QUERY = "update problem set num_submission = 0";
					stmt.executeUpdate(QUERY);
					QUERY = "update problem set num_correct = 0";
					stmt.executeUpdate(QUERY);
					System.out.println("");
				} catch(SQLException e) {
					System.out.println("SQLException : " + e);
				} finally {
					try {
						stmt.close();
						conn.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
			else if(inputNum == 0) {
				break;
			}
		}
	}
}
