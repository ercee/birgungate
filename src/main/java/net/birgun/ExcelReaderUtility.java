package net.birgun;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelReaderUtility {
    CellStyle cellStyle;

    public ExcelReaderUtility() {
	super();
    }

    public void readExcel() {
	PrintStream outEx = null;
	try {
	    outEx = new PrintStream(new FileOutputStream("output.txt"));
	    System.setOut(outEx);
	} catch (FileNotFoundException e2) {
	    e2.printStackTrace();
	}
	XSSFWorkbook workbook = new XSSFWorkbook();
	cellStyle = workbook.createCellStyle();
	CreationHelper createHelper = workbook.getCreationHelper();
	cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("d MMMM yyyy h:mm"));
	XSSFSheet sheet = workbook.createSheet("Total");
	List<Entity> entities;
	boolean success = true;
	try {
	    entities = getEntities();
	    entities.forEach(e -> e.setDate(e.getDate().minusHours(5)));
	    Map<LocalDate, Map<Integer, Map<ActionType, List<Entity>>>> idMap = entities.stream()
		    .collect(Collectors.groupingBy(e -> e.getDate().toLocalDate(), TreeMap::new,
			    Collectors.groupingBy(Entity::getId, Collectors.groupingBy(Entity::getAction))));
	    int rowNum = 0;
	    createHeaderRow(sheet, rowNum++);
	    for (Map<Integer, Map<ActionType, List<Entity>>> dateUserActionList : idMap.values()) {
		for (Map<ActionType, List<Entity>> userActionList : dateUserActionList.values()) {
		    List<Entity> inList = userActionList.get(ActionType.IN);
		    List<Entity> outList = userActionList.get(ActionType.OUT);
		    if (inList != null && outList != null) {
			TreeSet<Entity> inSet = new TreeSet<>(inList);
			Entity first = inSet.first();
			first.setDate(first.getDate().plusHours(5));
			TreeSet<Entity> outSet = new TreeSet<>(outList);
			Entity last = outSet.last();
			last.setDate(last.getDate().plusHours(5));
			XSSFRow row = sheet.createRow(rowNum++);
			createRowFromEntity(first, row);
			row = sheet.createRow(rowNum++);
			createRowFromEntity(last, row);
		    }
		}
	    }
	} catch (IOException e1) {
	    System.out.println(e1.getMessage() + " ilgili dosya bulunamadı veya kullanımda.");
	    success = false;
	}
	try {
	    FileOutputStream out = new FileOutputStream(new File(getPath("Giriş-Çıkış", ".xlsx")));
	    workbook.write(out);
	    workbook.close();
	    out.close();
	} catch (FileNotFoundException e) {
	    System.out.println(e.getMessage() + " ilgili dosya bulunamadı veya kullanımda.");
	    success = false;
	} catch (IOException e) {
	    System.out.println(e.getMessage() + " ilgili dosya bulunamadı veya kullanımda.");
	    success = false;
	}
	if (success)
	    System.out.println("Rapor başarıyla oluşturuldu.");
    }

    private void createRowFromEntity(Entity first, XSSFRow row) {
	XSSFCell cell = row.createCell(0);
	cell.setCellValue(first.getId());
	cell = row.createCell(1);
	cell.setCellValue(first.getName());
	cell = row.createCell(2);
	cell.setCellValue(first.getSurName());
	cell = row.createCell(3);
	cell.setCellValue(first.getAction().getProperty());
	cell = row.createCell(4);
	cell.setCellStyle(cellStyle);
	cell.setCellValue(Date.from(first.getDate().atZone(ZoneId.systemDefault()).toInstant()));
    }

    private void createHeaderRow(XSSFSheet sheet, int rowNum) {
	XSSFRow row = sheet.createRow(rowNum);
	XSSFCell cell = row.createCell(0);
	cell.setCellValue("ID");
	cell = row.createCell(1);
	cell.setCellValue("Ad");
	cell = row.createCell(2);
	cell.setCellValue("Soyad");
	cell = row.createCell(3);
	cell.setCellValue("Aksiyon");
	cell = row.createCell(4);
	cell.setCellValue("Tarih");
    }

    private List<Entity> getEntities() throws IOException {
	String path = getPath("input", ".txt");
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	List<Entity> entities = new LinkedList<>();
	List<String[]> lines = Files.lines(Paths.get(path), StandardCharsets.ISO_8859_1).map(line -> line.split("\\n+"))
		.collect(Collectors.toList());
	lines.remove(0);
	for (String[] lineArr : lines) {
	    Entity entity = new Entity();
	    String line = lineArr[0];
	    String[] columns = line.split("\\t+");
	    String dateStr = columns[0].trim();
	    String id = columns[1].trim();
	    int parseInt = Integer.parseInt(id);
	    entity.setId(parseInt);
	    LocalDateTime dateTime = LocalDateTime.parse(dateStr, formatter);
	    entity.setDate(dateTime);
	    String name = columns[2].trim();
	    entity.setName(name);
	    String surname = columns[3].trim();
	    entity.setSurName(surname);
	    String action = columns[5].trim();
	    entity.setAction("ÇIKIÞ".equals(action) ? ActionType.OUT : ActionType.IN);
	    entities.add(entity);
	}
	return entities;
    }

    private String getPath(String fileName, String fileExtension) {
	return System.getProperty("user.home") + "\\temp\\" + fileName + fileExtension;
    }

}
