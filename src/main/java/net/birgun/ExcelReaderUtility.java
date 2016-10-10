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
import java.util.LinkedHashMap;
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
    CellStyle cellStyleAsDate;
    CellStyle cellStyleAsTime;

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
	setStyles(workbook);
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
	    Map<Integer, List<EntityHolder>> nameBasedEntities = new LinkedHashMap<>();
	    for (Map<Integer, Map<ActionType, List<Entity>>> dateUserActionList : idMap.values()) {
		for (Map<ActionType, List<Entity>> userActionList : dateUserActionList.values()) {
		    List<Entity> inList = userActionList.get(ActionType.IN);
		    List<Entity> outList = userActionList.get(ActionType.OUT);
		    Entity first = null;
		    Entity last = null;
		    if (inList != null) {
			TreeSet<Entity> inSet = new TreeSet<>(inList);
			first = inSet.first();
			first.setDate(first.getDate().plusHours(5));

		    }
		    if (outList != null) {
			TreeSet<Entity> outSet = new TreeSet<>(outList);
			last = outSet.last();
			last.setDate(last.getDate().plusHours(5));
		    }
		    createRowFromEntity(first, last, sheet.createRow(rowNum++));
		    Integer id = first == null ? last.getId() : first.getId();
		    List<EntityHolder> list = nameBasedEntities.get(id);
		    if (list == null) {
			list = new LinkedList<>();
			nameBasedEntities.put(id, list);
		    }
		    list.add(new EntityHolder(first, last));
		}
	    }

	    for (List<EntityHolder> namedEntities : nameBasedEntities.values()) {
		XSSFWorkbook nameWorkbook = new XSSFWorkbook();
		setStyles(nameWorkbook);
		XSSFSheet nameSheet = nameWorkbook.createSheet("Total");
		int nameRow = 0;
		createHeaderRow(nameSheet, nameRow++);
		for (EntityHolder entityHolder : namedEntities) {
		    createRowFromEntity(entityHolder.getFirst(), entityHolder.getLast(),
			    nameSheet.createRow(nameRow++));
		}
		Entity first = namedEntities.get(0).getFirst();
		FileOutputStream out = new FileOutputStream(
			new File(getPath(first.getName() + first.getSurName(), ".xlsx")));
		nameWorkbook.write(out);
		nameWorkbook.close();
		out.close();
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

    private void setStyles(XSSFWorkbook workbook) {
	cellStyleAsDate = workbook.createCellStyle();
	CreationHelper createHelper = workbook.getCreationHelper();
	cellStyleAsDate.setDataFormat(createHelper.createDataFormat().getFormat("d MMMM yyyy"));
	cellStyleAsTime = workbook.createCellStyle();
	cellStyleAsTime.setDataFormat(createHelper.createDataFormat().getFormat("h:mm"));
    }

    private void createRowFromEntity(Entity first, Entity last, XSSFRow row) {
	XSSFCell cell = row.createCell(0);
	int id = first == null ? last.getId() : first.getId();
	cell.setCellValue(id);
	cell = row.createCell(1);
	String name = first == null ? last.getName() : first.getName();
	cell.setCellValue(name);
	cell = row.createCell(2);
	String surName = first == null ? last.getSurName() : first.getSurName();
	cell.setCellValue(surName);
	cell = row.createCell(3);
	cell.setCellStyle(cellStyleAsDate);
	LocalDateTime date = first == null ? last.getDate() : first.getDate();
	cell.setCellValue(Date.from(date.atZone(ZoneId.systemDefault()).toInstant()));
	cell = row.createCell(4);
	cell.setCellStyle(cellStyleAsTime);
	if (first != null) {
	    cell.setCellValue(Date.from(first.getDate().atZone(ZoneId.systemDefault()).toInstant()));
	} else {
	    cell.setCellValue("Giriş yok");
	}
	cell = row.createCell(5);
	cell.setCellStyle(cellStyleAsTime);
	if (last != null) {
	    cell.setCellValue(Date.from(last.getDate().atZone(ZoneId.systemDefault()).toInstant()));
	} else {
	    cell.setCellValue("Çıkış yok");
	}
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
	cell.setCellValue("Tarih");
	cell = row.createCell(4);
	cell.setCellValue("Giriş Saati");
	cell = row.createCell(5);
	cell.setCellValue("Çıkış Saati");
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
