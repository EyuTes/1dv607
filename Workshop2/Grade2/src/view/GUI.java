package view;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import model.Boat;
import model.Boat.BoatType;
import model.Member;
import model.Registry;

public class GUI implements Initializable, IView {

	//General fields of the program
	@FXML private Text welcomeText;
	@FXML private Button compactListButton;
	@FXML private Button verboListButton;	
	@FXML private Button createMemberButton;
	@FXML private Button saveAndQuitButton;
	
	//All fields are related to member
	@FXML private TableView<Member> memberTable;
	@FXML private TableColumn<Member, String> memberNameColumn;
	@FXML private TableColumn<Member, String> memberPersonalnumberColumn;
	@FXML private TableColumn<Member, String> memberIDColumn;
	@FXML private TableColumn<Member, String> memberTotalBoatsColumn;
	@FXML private TableColumn<Member, Member> memberBoatsInformationColumn;
	@FXML private TableColumn<Member, Member> memberEditColumn;
	@FXML private TableColumn<Member, Member> memberDeleteColumn;
	private TextField memberName = new TextField();
	private TextField memberPN = new TextField();
	
	//All fields are related to Boat
	@FXML private AnchorPane boatTablePane;
	@FXML private TableView<Boat> boatTable;
	@FXML private TableColumn<Boat, String> boatLengthColumn;
	@FXML private TableColumn<Boat, String> boatTypeColumn;
	@FXML private TableColumn<Boat, Boat> boatEditColumn;
	@FXML private TableColumn<Boat, Boat> boatDeleteColumn;
	private ChoiceBox <BoatType> boatTypeChoiceBox = new ChoiceBox<BoatType>(FXCollections.observableArrayList(BoatType.values())); 
	@FXML private Button addBoatButton;
	@FXML private Button closeBoatListButton;	
	private TextField boatLength = new TextField();
	
	private Registry registry;
	
	public GUI() {
		registry = new Registry();
		try {
			registry.readFiles();
		} catch (Exception e) {
			displayError("CANNOT READ THE FILES. PLEASE CHECK THE CONTENT OF \'Member.txt\' AND \'Boat.txt\' FILE.");
		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		startProgram();
		memberPN.setPromptText("YYYYMMDDXXXX");
		memberName.setPromptText("Eg: John Smith");
		boatLength.setPromptText("Eg: 14.65");
		compactListButton.setOnAction(e -> displayCompactList(registry.getMemberList()));
		verboListButton.setOnAction(e -> displayVerboseList(registry.getMemberList()));
		createMemberButton.setOnAction(e -> registerMember(memberName.getText(), memberPN.getText()));
		closeBoatListButton.setOnAction(e -> changeView());
		saveAndQuitButton.setOnAction(e -> quitProgram());
	}

	@Override
	public void startProgram() {
		displayWelcomeMessage();
		displayMainInstructions();
	}

	@Override
	public void displayWelcomeMessage() {
		welcomeText.setText("WELCOME TO MEMBER REGISTERY PROGRAM");
	}

	@Override
	public void displayMainInstructions() {
		compactListButton.setVisible(true);
		verboListButton.setVisible(true);
		createMemberButton.setVisible(true);
	}

	@Override
	public void displayCompactList(ArrayList<Member> m) {
		if(m.isEmpty()){
			displayError("List is Empty!!");
			return;
		}
		setMemberTable(m);
		memberPersonalnumberColumn.setVisible(false);
		memberBoatsInformationColumn.setVisible(false);
	}

	@Override
	public void displayVerboseList(ArrayList<Member> m) {
		if(m.isEmpty()){
			displayError("List is Empty!!");
			return;
		}
		setMemberTable(m);
		memberPersonalnumberColumn.setVisible(true);
		memberBoatsInformationColumn.setVisible(true);
	}

	@Override
	public void registerMember(String name, String personalnumber) {
		memberName.clear(); // clear the fields because we are registering member
		memberPN.clear();

		// We receive input inside the alert box.
		Alert alert = new Alert(AlertType.NONE);
		alert.setHeaderText("Register Member");
		alert.getButtonTypes().add(new ButtonType("Save"));
		alert.getButtonTypes().add(ButtonType.CANCEL);															//boatType button is null
		alert.getDialogPane().setContent(createDialogeBox(new Label("Name*"), memberName, new Label("Personal Number*"), memberPN, null));
		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == alert.getButtonTypes().get(0)) { 
			if (checkMemberDetails(memberName, memberPN)) {
				try {
					registry.registerMember(memberName.getText(), memberPN.getText());
					displaySuccess("Member Registered Successfully!!");
					setMemberTable(registry.getMemberList());
				} catch (ParseException e) {
					displayError("Please fill the correct personal number date format. Eg: YYYYMMDDXXXX!!");
				}
			}
		} else
			alert.close();
	}

	@Override
	public void updateMember(Member m, String name, String personalnumber) {
		memberName.setText(name); //It sets given name and personal number in the fields.
		memberPN.setText(personalnumber);
		
		Alert alert = new Alert(AlertType.NONE);
		alert.setHeaderText("Update Member");
		alert.getButtonTypes().add(new ButtonType("Save"));
		alert.getButtonTypes().add(ButtonType.CANCEL);														//boatType button is null
		alert.getDialogPane().setContent(createDialogeBox(new Label("Name*"), memberName, new Label("Personal Number*"), memberPN, null));
		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == alert.getButtonTypes().get(0)) { 
			if (checkMemberDetails(memberName, memberPN)) {
				try {
					registry.updateMember(m, memberName.getText(), memberPN.getText());
					displaySuccess("Member Updated Successfully !!");
					memberTable.refresh();
				} catch (ParseException e) {
					displayError("Please fill the correct personal number date format. Eg: YYYYMMDDXXXX!!");
				}
			}
		} else
			alert.close();
	}

	@Override
	public void deleteMember(Member m) {
		Alert alert = new Alert(AlertType.CONFIRMATION, "Are you sure?");
		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == alert.getButtonTypes().get(0)) {
			registry.deleteMember(m);
			displaySuccess("Member Deleted Successfully!!");
			setMemberTable(registry.getMemberList());
		} else
			alert.close();
	}

	@Override
	public void displaySelectedMember(Member m) {
		//Since we are using the same scene for displaying members information, it hides current buttons and tables.
		memberTable.setVisible(false);
		compactListButton.setVisible(false);
		verboListButton.setVisible(false);
		createMemberButton.setVisible(false);
		
		//Hereby display selected member´s boats.
		setBoatTable(m);
		boatTablePane.setVisible(true);
		//set register boat button on action by giving default values.
		addBoatButton.setOnAction(e -> registerBoat(m, 0, null));
	}

	@Override
	public void registerBoat(Member m, double boatLength, BoatType boattype) {
		this.boatLength.clear(); // because we are registering, so we need to clear previous inputs.
		boatTypeChoiceBox.getSelectionModel().select(BoatType.Sailboat); // default selection.

		Alert alert = new Alert(AlertType.NONE);
		alert.setHeaderText("Register Boat");
		alert.getButtonTypes().add(new ButtonType("Save"));
		alert.getButtonTypes().add(ButtonType.CANCEL);													//personal number field is null
		alert.getDialogPane().setContent(createDialogeBox(new Label("Length(m)*"), this.boatLength, new Label("Type*"), null, boatTypeChoiceBox));
		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == alert.getButtonTypes().get(0)) {
			if (checkBoatLength(this.boatLength)) { 
				try {
					m.registerBoat(Double.parseDouble(this.boatLength.getText()), boatTypeChoiceBox.getSelectionModel().getSelectedItem());
					displaySuccess("Boat Registerd Succesfully!!");
					setBoatTable(m);
				} catch (Exception e) {
					displayError("Please fill the correct boat length. Eg: 14.23");
				}
			}
		} else
			alert.close();
	}

	@Override
	public void updateBoat(Member m, double length, BoatType type, Boat boat) {
		boatLength.setText("" + length); //text fields only take strings.
		boatTypeChoiceBox.getSelectionModel().select(type); // set the present type of given boat
		
		Alert alert = new Alert(AlertType.NONE);
		alert.setHeaderText("Update Boat");
		alert.getButtonTypes().add(new ButtonType("Save"));
		alert.getButtonTypes().add(ButtonType.CANCEL);
		alert.getDialogPane().setContent(createDialogeBox(new Label("Length(m)"), boatLength, new Label("Type"), null, boatTypeChoiceBox));
		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == alert.getButtonTypes().get(0)) {
			if (checkBoatLength(this.boatLength)) { 
				try {
					m.updateBoat(Double.parseDouble(this.boatLength.getText()), boatTypeChoiceBox.getSelectionModel().getSelectedItem(), boat);
					displaySuccess("Boat Updated Successfully!!");
					setBoatTable(m);
				} catch (Exception e) {
					displayError("Please fill the correct boat length. Eg: 14.23");
				}
			}
		} else
			alert.close();
	}

	@Override
	public void deleteBoat(Member m, Boat b) {
		Alert alert = new Alert(AlertType.CONFIRMATION, "Are you sure?");
		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == alert.getButtonTypes().get(0)) {
			m.deleteBoat(b);
			displaySuccess("Boat Deleted Successfully!!");
			setBoatTable(m);  // update the table
		} else
			alert.close();
	}

	@Override
	public void displayError(String error) {
		Alert alert = new Alert(AlertType.ERROR, error);
		alert.show();
	}

	@Override
	public void displaySuccess(String success) {
		Alert alert = new Alert(AlertType.INFORMATION, success);
		alert.show();
	}

	@Override
	public void quitProgram() {
		try {
			registry.saveRegistry();
			displaySuccess("REGISTRY SAVED SUCCESSFULLY!!.");	
			System.exit(1);
		} catch (IOException e) {
			displayError("ERROR!! REGISTRY FILE IS NOT SAVED.");
		}
	}

	/********************************** FOR CREATING VIEW ************************/
	private void changeView() {
		//For displaying the member table, we have to hide boat table
		boatTablePane.setVisible(false);
		compactListButton.setVisible(true);
		verboListButton.setVisible(true);
		createMemberButton.setVisible(true);
		memberTable.setVisible(true);
		setMemberTable(registry.getMemberList()); // update table
	}

	// This create a dialogue box for getting the input from user
	private GridPane createDialogeBox(Label l1, TextField name, Label l2, TextField pn, ChoiceBox<BoatType> boatTypes) {
		GridPane pane = new GridPane();
		pane.setHgap(10);
		pane.setVgap(10);
		pane.setPadding(new Insets(20, 150, 10, 10));
		pane.add(l1, 0, 0);
		pane.add(name, 1, 0);
		pane.add(l2, 0, 1);
		if (pn == null) // if we need to show boat types button
			pane.add(boatTypes, 1, 1);
		else
			pane.add(pn, 1, 1);
		return pane;
	}

	//display the boats of given member
	private void setBoatTable(Member member) {
		ObservableList<Boat> data = FXCollections.observableArrayList(member.getBoatList());
		boatLengthColumn.setCellValueFactory(new PropertyValueFactory<Boat, String>("length"));
		boatTypeColumn.setCellValueFactory(new PropertyValueFactory<Boat, String>("type"));
		boatEditColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue())); //for adding edit buttons
		boatEditColumn.setCellFactory(param -> new TableCell<Boat, Boat>() {
			private final Button boatEditButton = new Button("Edit");

			@Override
			protected void updateItem(Boat boat, boolean empty) {
				super.updateItem(boat, empty);
				if (boat == null) {
					setGraphic(null);
					return;
				} else {
					setGraphic(boatEditButton);
					boatEditButton.setOnAction(event -> updateBoat(member, boat.getLength(), boat.getType(), boat)); //set on action
				}
			}
		});
		boatDeleteColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue())); //for adding delete buttons
		boatDeleteColumn.setCellFactory(param -> new TableCell<Boat, Boat>() {
			private final Button boatDeleteButton = new Button("Delete");

			@Override
			protected void updateItem(Boat boat, boolean empty) {
				super.updateItem(boat, empty);
				if (boat == null) {
					setGraphic(null);
					return;
				} else {
					setGraphic(boatDeleteButton);
					boatDeleteButton.setOnAction(event -> deleteBoat(member, boat));
				}
			}
		});
		boatTable.setItems(data);
	}

	//display given member list
	private void setMemberTable(ArrayList<Member> m) {
		ObservableList<Member> data = FXCollections.observableArrayList(m);
		memberNameColumn.setCellValueFactory(new PropertyValueFactory<Member, String>("name"));
		memberPersonalnumberColumn.setCellValueFactory(new PropertyValueFactory<Member, String>("personalnumber"));
		memberIDColumn.setCellValueFactory(new PropertyValueFactory<Member, String>("memberID"));
		memberTotalBoatsColumn.setCellValueFactory(new PropertyValueFactory<Member, String>("numberOfBoats"));
		memberBoatsInformationColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
		memberBoatsInformationColumn.setCellFactory(param -> new TableCell<Member, Member>() {
			private final Button viewBoats = new Button("View Boats");

			@Override
			protected void updateItem(Member member, boolean empty) {
				super.updateItem(member, empty);
				if (member == null) {
					setGraphic(null);
					return;
				} else {
					setGraphic(viewBoats);
					viewBoats.setOnAction(event -> displaySelectedMember(member));
				}
			}
		});
		memberEditColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
		memberEditColumn.setCellFactory(param -> new TableCell<Member, Member>() {
			private final Button editButton = new Button("Edit");

			@Override
			protected void updateItem(Member member, boolean empty) {
				super.updateItem(member, empty);
				if (member == null) {
					setGraphic(null);
					return;
				} else {
					setGraphic(editButton);
					editButton.setOnAction(e -> updateMember(member, member.getName(), member.getPersonalnumber()));
				}
			}
		});
		memberDeleteColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
		memberDeleteColumn.setCellFactory(param -> new TableCell<Member, Member>() {
			private final Button deleteButton = new Button("Delete");

			@Override
			protected void updateItem(Member member, boolean empty) {
				super.updateItem(member, empty);
				if (member == null) {
					setGraphic(null);
					return;
				} else {
					setGraphic(deleteButton);
					deleteButton.setOnAction(event -> deleteMember(member));
				}
			}
		});

		memberTable.setItems(data);
		memberTable.setVisible(true);
	}

	/*************************** FOR CHECKING INPUT *************************************************/
	private boolean checkBoatLength(TextField length) {
		if(length.getText().isEmpty()){
			displayError("Boat length field is empty");
			return false;
		}
		try {
			Double.parseDouble(length.getText());
		} catch (Exception e) {
			displayError("Please fill the correct boat length. Eg: 14.23");
			return false;
		}
		return true;
	}

	private boolean checkName(TextField name) {
		if (name.getText().isEmpty()) 
			return false;
		boolean charexists = false;
		for (int i = 0; i < name.getText().length(); i++) {
			char c = name.getText().charAt(i);
			if (Character.isAlphabetic(c))
				charexists = true;
			if (!Character.isAlphabetic(c) && !Character.isWhitespace(c))
				return false;
		}
		return charexists;
	}

	private boolean checkMemberDetails(TextField name, TextField pn) {
		if(name.getText().isEmpty() && pn.getText().isEmpty()){
			displayError("Please fill all the required(*) fields.");
			return false;
		}
		else if (name.getText().isEmpty()){
			displayError("Name field is empty.");
			return false;
		}
		else if (pn.getText().isEmpty()){
			displayError("Personal number field is empty.");
			return false;
		}
		else if (!(checkName(name))){
			displayError("Please fill the correct name. Eg: John Smith!!");
			return false;
		}
		else if (pn.getText().length() != 12){
			displayError("Personal number contains 12 digits only. Eg: YYYYMMDDXXXX");
			return false;
		}
			return true;
	}
}