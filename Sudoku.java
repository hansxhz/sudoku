import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class Sudoku {
	public static final int NA = 9999;
	public static final String NUMBERS = "0123456789";
	public static final int[] MASK = {8176, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096};
	public static void main(String[] args) {
		ms = System.currentTimeMillis();
		logOpen();
        try {
        	if (args.length > 1) {
        		ip = args[0];
            	op = args[1];
        	}
        } catch (ArrayIndexOutOfBoundsException e) {}
        File d = new File(ip);
        if (!d.isDirectory()) exit(ip + " is not a directory.", 4);
        String[] l = d.list();
        if (l.length < 1) exit(ip + " is empty.", 4);
		int[][] m = new int[9][9];
		for (int i = 0; i < l.length; i++)
			if (load(ip + "\\" + l[i], m)) {
				hm.put(l[i], new Thread(new Game(l[i], m)));
				hm.get(l[i]).start();
			}
		if (hm.isEmpty()) exit("No valid game.", 0);
    }
	public static synchronized void complete(String key, boolean done, int[][] n) {
		if (!hm.containsKey(key)) return;
		if (done) {
			if (!save(op + "\\" + key + ".answer", n)) logln("Failed to save file for " + key);
			for (int i = 0; i < 9; i++) {
				for (int j = 0; j < 9; j++)
	    			log(" " + n[i][j]);
				logln();
			}
		} else logln("Failed to solve " + key);
		hm.remove(key);
		if (hm.isEmpty())
			exit("Total: " + (System.currentTimeMillis() - ms) + "ms", 0);
	}
	public static int base(int i) {
		return i - i % 3;
	}
	public static boolean in(int n, int v) {
		return (n & MASK[v]) > 0;
	}
	public static int count(int n) {
		int c = 0;
		for (int i = 1; i < 10; i++) if (in(n, i)) c++;
		return c;
	}
    public static boolean unique(int i, int j, int v, int[][] n) {
    	for (int k = 0; k < 9; k++) {
    		if (n[i][k] == v && k != j) return false;
    		if (n[k][j] == v && k != i) return false;
    	}
    	int a = base(i);
    	int b = base(j);
    	for (int x = a; x < a+3; x++)
        	for (int y = b; y < b+3; y++)
        		if (n[x][y] == v && (x != i || y != j)) return false;
    	return true;
    }
    public static void log(String s) {
		try {
	        lf.write(s.getBytes());
        } catch (IOException e) {
        	System.err.println(e.getMessage());
        }
    }
    public static void logln(String s) {
		log(s + "\r\n");
    }
    public static void logln() {
		log("\r\n");
    }
    public static boolean load(String fn, int[][] n) {
        boolean b = true;
        try {
	        FileInputStream f = new FileInputStream(fn);
			for (int i = 0; i < 9; i++) {
				for (int j = 0; j < 9; j++) {
					n[i][j] = (f.read() - NUMBERS.charAt(0));
					b = (n[i][j] >= 0 && n[i][j] <= 9);
					if (!b) break;
					if (n[i][j] == 0) n[i][j] = MASK[0];
				}
				if (!b) break;
			}
	        f.close();
        } catch (IOException e) {
        	System.err.println(e.getMessage());
        	return false;
        }
		return b;
	}
    public static boolean save(String fn, int[][] n) {
		try {
	        FileOutputStream f = new FileOutputStream(fn);
			for (int i = 0; i < 9; i++)
				for (int j = 0; j < 9; j++)
					f.write(NUMBERS.charAt(n[i][j]));
	        f.close();
        } catch (IOException e) {
        	System.err.println(e.getMessage());
        	return false;
        }
		logln("Saved to " + fn);
        return true;
    }
	private static void exit(String s, int i) {
		logln(s);
		logClose();
        System.out.println(s);
        System.exit(i);
	}
	private static void logOpen() {
		try {
	        lf = new FileOutputStream("sudoku.log");
        } catch (IOException e) {
        	System.err.println(e.getMessage());
        }
    }
	private static void logClose() {
		try {
	        lf.close();
        } catch (IOException e) {
        	System.err.println(e.getMessage());
        }
    }
	private static long ms;
	private static String ip = "in";
	private static String op = "out";
	private static FileOutputStream lf;
	private static HashMap<String, Thread> hm = new HashMap<String, Thread>();
}

class Game implements Runnable {
	public Game(String s, int[][] n) {
		for (int i = 0; i < 9; i++)
			for (int j = 0; j < 9; j++)
				nodes[i][j] = n[i][j];
		title = s;
	}
    @Override
    public void run() {
    	prepare();
    	foreplay();
        for (int i = 0; i < 2; i++)
        	new Thread(new Hardcore(this, i > 0)).start();
    }
    public String key(){
    	return title;
    }
	public int get(int i, int j) {
		return nodes[i][j];
	}
	protected void prepare() {
		for (int i = 0; i < 9; i++)
			for (int j = 0; j < 9; j++) {
				if (nodes[i][j] < 10) continue;
				for (int v = 1; v < 10; v++)
					if (Sudoku.in(nodes[i][j], v))
						if (!Sudoku.unique(i, j, v, nodes))
							remove(i, j, v);
			}
		singles();
	}
	protected void foreplay() {
		while (true) {
			int x, y, v, c = 0;
			for (x = 0; x < 3; x++) {
				for (y = 0; y < 3; y++) {
					for (v = 1; v < 10; v++)
						if (mapping(x, y, v)) c += reduce();
					mapping(x, y);
					while (true) {
						for (v = 0; v < 4; v++)
							if (pick(2 + v/2, v%2 > 0)) break;
						if (v == 4) break;
						c += reduce();
					}
				}
			}
			for (v = 1; v < 10; v++)
				if (mapping(v) > 3)
					for (x = 0; x < 3; x++) {
						if (map2go(x, false)) c += reduce();
						if (map2go(x, true)) c += reduce();
					}
			if (c < 1) break;
		}
	}
	protected int reduce() {
		int c = slim();
		if (c > 0) c += singles();
		return c;
	}
	protected int singles() {
		int c = 0;
		while (true) {
			int p = c;
			for (int i = 0; i < 9; i++)
				for (int j = 0; j < 9; j++) {
					if (nodes[i][j] < 10) continue;
					for (int v = 1; v < 10; v++)
						if (nodes[i][j] == Sudoku.MASK[v]) {
							vn = 1;
							sv = v;
							addNode(i, j, 0);
							nodes[i][j] = v;
							c += slim();
						}
				}
			if (c == p) return c;
		}
	}
	protected int slim() {
		if (vn < 1) return 0;
		int a, b, i, j, x, y;
		x = ns[0][0];
		y = ns[0][1];
		a = Sudoku.base(x);
		b = Sudoku.base(y);
		for (i = 1; i < vn; i++) {
			if (Sudoku.base(ns[i][0]) != a || Sudoku.base(ns[i][1]) != b) return 0;
			if (ns[i][0] != x) x = Sudoku.NA;
			if (ns[i][1] != y) y = Sudoku.NA;
		}
		if (sv > 0 && x == Sudoku.NA && y == Sudoku.NA) return 0;
		int c = 0;
		if (vn == 1) {
			c += Sudoku.count(nodes[x][y]);
			nodes[x][y] = sv;
		}
		for (i = a; i < a+3; i++)
			for (j = b; j < b+3; j++)
				c += clean(i, j);
		for (i = 0; i < 9; i++) {
			if (x != Sudoku.NA && Sudoku.base(i) != b) c += clean(x, i);
			if (y != Sudoku.NA && Sudoku.base(i) != a) c += clean(i, y);
		}
		return c;
	}
	protected int clean(int i, int j) {
		if (nodes[i][j] < 10) return 0;
		boolean inner = false;
		int v, k;
		for (k = 0; k < vn; k++)
			if (ns[k][0] == i && ns[k][1] == j) {
				inner = true;
				break;
			}
		if (sv > 0 && inner) return 0;
		int c = 0;
		for (v = 1; v < 10; v++)
			if (Sudoku.in(nodes[i][j], v)) {
				if (sv == 0) {
					for (k = 0; k < vn; k++)
						if (va[k] == v) {
							if (!inner) c += remove(i, j, v);
							break;
						}
					if (inner && k == vn) c += remove(i, j, v);
				} else if (v == sv) c += remove(i, j, v);
			}
		return c;
	}
	protected int remove(int i, int j, int v) {
		if (!Sudoku.in(nodes[i][j], v)) return 0;
		nodes[i][j] &= ~Sudoku.MASK[v];
		return 1;
	}
	protected boolean mapping(int x, int y, int v) {
		if (v < 1 || v > 9) return false;
		vn = 0;
		sv = v;
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 3; j++) {
				map[v][i*3+j+1] = Sudoku.in(nodes[i+x*3][j+y*3], v)? (i+x*3)*9+j+y*3 : Sudoku.NA;
				if (map[v][i*3+j+1] != Sudoku.NA) {
					map[0][i*3+j+1] = Sudoku.count(nodes[i+x*3][j+y*3]);
					addNode(x*3+i, y*3+j, vn);
					vn++;
				}
			}
		map[v][0] = vn;
		return vn > 0;
	}
	protected void mapping(int x, int y) {
		for (int i = 0; i < 10; i++) map[0][i] = 0;
		for (int v = 1; v < 10; v++) mapping(x, y, v);
	}
	protected int mapping(int v) {
		if (v < 1 || v > 9) return 0;
		int c = 0;
		for (int i = 0; i < 9; i++)
			for (int j = 0; j < 9; j++) {
				map[i][j] = Sudoku.in(nodes[i][j], v)? v : 0;
				if (map[i][j] > 0) c++;
			}
		return c;
	}
	protected boolean map2go(int n, boolean vertical) {
		int[][] m = new int[3][3];
		int[] c = {0, 0, 0};
		int i, j, k, a, b;
		i = j = 0;
		for (a = 0; a < 3; a++)
			for (b = 0; b < 3; b++) {
				if (vertical) j = n*3+b;
				else i = n*3+a;
				m[a][b] = 0;
				for (k = 0; k < 3; k++) {
					if (vertical) i = a*3+k;
					else j = b*3+k;
					if (map[i][j] > 0) {
						m[a][b] = map[i][j];
						if (vertical) c[a]++;
						else c[b]++;
						break;
					}
				}
			}
		for (i = 0; i < 2; i++) {
			if (c[i] != 2) continue;
			for (j = i+1; j < 3; j++) {
				if (c[j] != 2) continue;
				for (k = 0; k < 3; k++)
					if (vertical) {
						if (m[i][k] != m[j][k] ) break;
					} else {
						if (m[k][i] != m[k][j] ) break;
					}
				if (k == 3) break;
			}
			if (j < 3) break;
		}
		if (i == 2) return false;
		for (k = 0; k < 3; k++) if (k != i && k != j) break;
		sv = 0;
		for (a = 0; a < 3; a++) {
			b = vertical? m[i][a] : m[a][i];
			if (b > 0) {
				b = vertical? m[k][a] : m[a][k];
				if (b > 0) sv = b;
			} else j = a;
		}
		if (sv < 1) return false;
		vn = 0;
		for (i = 0; i < 3; i++) {
			a = vertical? k*3+i : n*3+j;
			b = vertical? n*3+j : k*3+i;
			if (map[a][b] > 0) {
				addNode(a, b, vn);
				vn++;
			}
		}
		return vn > 0;
	}
	protected void addNode(int i, int j, int k) {
		ns[k][0] = i;
		ns[k][1] = j;
	}
	protected void addNode(int j, int k) {
		for (int v = 1; v < 10; v++)
			if (map[v][j] != Sudoku.NA) {
				ns[k][0] = map[v][j] / 9;
				ns[k][1] = map[v][j] % 9;
				return;
			}
	}
	protected boolean pick(int n, boolean node) {
		if (n < 2 || n > 3) return false;
		int v, x, y, k, c = 0;
		int[] a = new int[9];
		for (x = n; x > 1; x--)
			for (v = 1; v < 10; v++)
				if (mapCount(v, node) == x) {
					a[c] = v;
					c++;
				}
		if (c < n) return false;
		for (int i = 0; i < c-n+1; i++) {
			if (mapCount(a[i], node) < n) return false;
			int[] s = new int[9];
			int[] z = new int[3];
			s[0] = a[i];
			x = select(a[i], 0, node);
			for (y = 0; y < n; y++) {
				z[y] = mapValue(a[i], y, node);
				if (node) va[y] = z[y];
				else addNode(z[y], y);
			}
			for (int j = i+1; j < c; j++) {
				for (k = 0; k < mapCount(a[j], node); k++) {
					v = mapValue(a[j], k, node);
					for (y = 0; y < n; y++) if (z[y] == v) break;
					if (y == n) break;
				}
				if (k == mapCount(a[j], node)) {
					s[x] = a[j];
					x = select(a[j], x, node);
				}
			}
			if (x == n) {
				vn = n;
				sv = 0;
				for (k = 0; k < n; k++)
					if (node) map[0][s[k]] = 0;
					else map[s[k]][0] = 0;
				return true;
			}
		}
		return false;
	}
	protected int select(int k, int i, boolean node) {
		if (node) addNode(k, i);
		else va[i] = k;
		return i + 1;
	}
	protected int mapCount(int v, boolean node) {
		return node? map[0][v] : map[v][0];
	}
	protected int mapValue(int j, int k, boolean node) {
		int v, i = k;
		for (v = 1; v < 10; v++) {
			int x = node? map[v][j] : map[j][v];
			if (x != Sudoku.NA) {
				if (i == 0) return v;
				i--;
			}
		}
		return Sudoku.NA;
	}
	protected String title;
	protected int[][] nodes = new int[9][9];
	protected int[][] map = new int[10][10];
	protected int[][] ns = new int[9][2];
	protected int[] va = new int[9];
	protected int vn, sv;
}

class Hardcore implements Runnable {
	public Hardcore(Game p, boolean b) {
		oya = p;
		reverse = b;
		for (int i = 0; i < 9; i++)
			for (int j = 0; j < 9; j++) {
				m[i][j] = p.get(i, j) & (Sudoku.MASK[1] - 1);
				w[i*9+j][0] = p.get(i, j) & Sudoku.MASK[0];
				w[i*9+j][1] = Sudoku.NA;
			}
	}
    @Override
    public void run() {
		for (int l = 0; l < 81; l++)
			if (w[l][0] > 0)
				if (!mark(l))
					l = back(l);
    	Sudoku.complete(oya.key(), done(), m);
	}
    private boolean mark(int l) {
    	int v, i = reverse? -1 : 1;
    	if (w[l][1] == Sudoku.NA) v = reverse? 9 : 1;
    	else v = w[l][1] + i;
    	while (v > 0 && v < 10) {
    		if(Sudoku.in(w[l][0], v))
        		if (Sudoku.unique(l/9, l%9, v, m))
        			return valid(l, v);
    		v += i;
    	}
    	return valid(l, 0);
    }
    private boolean valid(int l, int v) {
		m[l/9][l%9] = v;
		w[l][1] = v > 0? v : Sudoku.NA;
		return v > 0;
    }
    private int back(int l) {
    	for (int i = l-2; i > -2; i--)
    		if (w[i+1][0] > 0) return i;
    	return Sudoku.NA;
    }
    private boolean done() {
		for (int i = 0; i < 9; i++)
			for (int j = 0; j < 9; j++)
				if (m[i][j] < 1 || m[i][j] > 9) return false;
    	return true;
    }
    private Game oya;
	private boolean reverse;
	private int[][] m = new int[9][9];
	private int[][] w = new int[81][2];
}