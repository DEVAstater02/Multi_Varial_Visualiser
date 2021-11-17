package com.example.multivarialvisualiser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.text.SpannableStringBuilder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.internal.NavigationMenu;
import com.google.android.material.internal.NavigationMenuPresenter;
import com.google.android.material.internal.NavigationMenuView;
import com.google.android.material.navigation.NavigationView;

import org.w3c.dom.Text;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.mariuszgromada.math.mxparser.*;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    //private variable for the main screen toolbar
    private Toolbar toolbar;
    //drawerlayout variable for making use of the hamburger navigation view
    DrawerLayout hamDrawerLayout;
    DrawerLayout calcDrawerLayout;
    //hamburger navigation view variable
    NavigationView hamNavView;
    //calc navigation view variable which contains the scientific functions
    NavigationView calcNavView;

    //TextView variables for the calculator workings and results
    EditText workingsTV;
    TextView resultsTV;

    ImageView calcDrawView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //func to initialising workingsTV and resultsTV
        initTextViews();

        //to prevent the keyboard from popping up
        workingsTV.setShowSoftInputOnFocus(false);

        hamDrawerLayout = findViewById(R.id.drawer_layout);
        calcDrawerLayout = findViewById(R.id.calc_drawer_layout);
        hamNavView = findViewById(R.id.ham_view);
        calcNavView = findViewById(R.id.calc_view);

        //toolbar declaration
        hamNavView.bringToFront();
        calcNavView.bringToFront();
        toolbar=findViewById(R.id.appBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);//disabling the title display on the tool bar to false

        //drawer toggle set up and onClickListener for the hamburger button
        ActionBarDrawerToggle hamToggle = new ActionBarDrawerToggle(this, hamDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        hamDrawerLayout.addDrawerListener(hamToggle);
        hamToggle.syncState();

        calcDrawView = (ImageView) findViewById(R.id.scientific_calc_button);
        calcDrawView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calcDrawerLayout.openDrawer(GravityCompat.END);
            }
        });

        //func calls to make the items in the navigation menu clickable
        hamNavView.setNavigationItemSelectedListener(this);
        hamNavView.setCheckedItem(R.id.home);

        //Function to disable the scrollbar in the calc navigation view
        disableNavigationViewScrollbars(calcNavView);
        //func to hide the home button in the hamburger menu
        hideMenuItem();
    }

    //func to initialize results TextView and the workings EditText containers
    private void initTextViews() {
        workingsTV = (EditText) findViewById(R.id.workingsTV);
        resultsTV = (TextView) findViewById(R.id.resultsTV);
    }

    //function to disable the scroll mode in the calc navigation view
    private void disableNavigationViewScrollbars(NavigationView navigationView){
        if(navigationView != null){
            NavigationMenuView navigationMenuView = (NavigationMenuView) navigationView.getChildAt(0);
            navigationMenuView.setLayoutManager(new LinearLayoutManager(this));
            if(navigationMenuView != null){
                navigationMenuView.setVerticalScrollBarEnabled(false);
            }
        }
    }

    //Function to prevent the app from closing when the back button is pressed when in the navigation view
    @Override
    public void onBackPressed() {
        if(hamDrawerLayout.isDrawerOpen(GravityCompat.START) || calcDrawerLayout.isDrawerOpen(GravityCompat.END)){
            if(hamDrawerLayout.isDrawerOpen(GravityCompat.START))
                hamDrawerLayout.closeDrawer(GravityCompat.START);
            else
                calcDrawerLayout.closeDrawer(GravityCompat.END);
        }
        else{
            super.onBackPressed();
        }
    }

    //function to register the clicks in the hamburger navView and move to the respective activity
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.home:
                break;

            case R.id.explicit_2d:
                Intent intent = new Intent(MainActivity.this, DesmosActivity.class);
                startActivity(intent);
                break;

            case R.id.explicit_3d:
                Toast.makeText(this, "Share", Toast.LENGTH_SHORT).show();
                break;
        }

        hamDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    //func description to hide a menu item
    private void hideMenuItem(){
        Menu hamMenu = hamNavView.getMenu();
        hamMenu.findItem(R.id.home).setVisible(false);
    }

    //func to update the math equation
    private void setWorkings(String strToAdd){
        String oldStr = workingsTV.getText().toString();
        int cursorPos = workingsTV.getSelectionStart();
        String leftStr = oldStr.substring(0,cursorPos);
        String rightStr = oldStr.substring(cursorPos);

        workingsTV.setText(String.format("%s%s%s",leftStr, strToAdd, rightStr));
        workingsTV.setSelection(cursorPos + strToAdd.length());
    }

    //The onClick functions for the calculator buttons
    public void backButton(View view) {
        int cursorPos = workingsTV.getSelectionStart();
        int textLength = workingsTV.getText().length();

        if(cursorPos != 0 && textLength != 0){
            SpannableStringBuilder selection = (SpannableStringBuilder) workingsTV.getText();
            selection.replace(cursorPos-1,cursorPos, "");
            workingsTV.setText(selection);
            workingsTV.setSelection(cursorPos-1);
        }

        if(textLength == 0){
            resultsTV.setText("0");
            workingsTV.setText("");
        }
    }

    public void equateButton(View view) {
        String userExp = workingsTV.getText().toString();

        //coded to replace the typical symbols with the actual parser symbols
        userExp = userExp.replaceAll(getResources().getString(R.string.multiplyText), "*");
        userExp = userExp.replaceAll(getResources().getString(R.string.divideText), "/");
        userExp = userExp.replaceAll("%","#");
        userExp = userExp.replaceAll("√","sqrt");
        userExp = userExp.replaceAll("log","log10");
        userExp = userExp.replaceAll("π", "pi");

        Expression exp = new Expression(userExp);
        String result = String.valueOf(exp.calculate());

        //displays the result in the TextView
        resultsTV.setText(result);
    }

    public void insertZero(View view) {
        setWorkings(getResources().getString(R.string.zeroText));
    }

    public void insertOne(View view) {
        setWorkings(getResources().getString(R.string.oneText));
    }

    public void insertTwo(View view) {
        setWorkings(getResources().getString(R.string.twoText));
    }

    public void insertThree(View view) {
        setWorkings(getResources().getString(R.string.threeText));
    }

    public void insertFour(View view) {
        setWorkings(getResources().getString(R.string.fourText));
    }

    public void insertFive(View view) {
        setWorkings(getResources().getString(R.string.fiveText));
    }

    public void insertSix(View view) {
        setWorkings(getResources().getString(R.string.sixText));
    }

    public void insertSeven(View view) {
        setWorkings(getResources().getString(R.string.sevenText));
    }

    public void insertEight(View view) {
        setWorkings(getResources().getString(R.string.eightText));
    }

    public void insertNine(View view) {
        setWorkings(getResources().getString(R.string.nineText));
    }

    public void insertDecimal(View view) {
        setWorkings(getResources().getString(R.string.decimalText));
    }

    public void addButton(View view) {
        setWorkings(getResources().getString(R.string.addText));
    }

    public void subButton(View view) {
        setWorkings(getResources().getString(R.string.subText));
    }

    public void mulButton(View view) {
        setWorkings(getResources().getString(R.string.multiplyText));
    }

    public void divButton(View view) {
        setWorkings(getResources().getString(R.string.divideText));
    }

    public void trigSinButton(View view){
        setWorkings("sin(");
    }

    public void trigCosButton(View view){
        setWorkings("cos(");
    }

    public void trigTanButton(View view){
        setWorkings("tan(");
    }

    public void trigArcSinButton(View view){
        setWorkings("arcsin(");
    }

    public void trigArcCosButton(View view){
        setWorkings("arccos(");
    }

    public void trigArcTanButton(View view){
        setWorkings("arctan(");
    }

    public void logButton(View view){
        setWorkings("log(");
    }

    public void lonButton(View view){
        setWorkings("ln(");
    }

    public void sqrtButton(View view){
        setWorkings("√(");
    }

    public void openingBracButton(View view){
        setWorkings("(");
    }

    public void closingBracButton(View view){
        setWorkings(")");
    }

    public void piButton(View view){
        setWorkings("π");
    }

    public void eButton(View view){
        setWorkings("e");
    }

    public void xSquareButton(View view){
        setWorkings("^(2)");
    }

    public void xPoweryButton(View view){
        setWorkings("^(");
    }

    public void primeButton(View view){
        setWorkings("ispr(");
    }

    public void modulusButton(View view){
        setWorkings("abs(");
    }

    public void percentageButton(View view){
        setWorkings("%");
    }
}