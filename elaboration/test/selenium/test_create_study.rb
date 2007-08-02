require File.dirname(__FILE__) + '/test_helper'
require "test/unit"


class CreateStudyTest < Test::Unit::TestCase
  include StudyCalendar::SeleniumCommands
  def test_create_study
    
    login()
    create_new_study()
    name_study()
  end
  def create_new_study
    assert_page_contains("Create a new template")
    click_link_with_text("Create a new template")
    wait_for_page_to_load "30000"
    assert_page_contains("Patient Study Calendar")
    assert_page_contains("Template for")
    assert_element_exists("study-name", false)
    assert_page_contains("Mark this template complete")
    assert_page_contains("Assign sites")
    assert_element_exists("epochs")
    assert_element_exists("selected-arm")
    assert_element_exists("study-name")
  end
  def name_study() 
    #Tests that name can be change with a click on the current name
    click_element("study-name")
    type_in_lone_text_field("TEST1")
    click_button_with_text("OK", 'submit')
    @name_study = "TEST1"
    wait_for_condition_text("TEST1")
    assert_page_contains("TEST1")
    
    
    #Tests that name can be changed with a click on 'Set name'
    click_link_with_text("Set name")
    type_in_lone_text_field("TEST2")
    click_button_with_text("OK", 'submit')
      wait_for_condition_text("TEST2")    
      @name_study= "TEST2"
    assert_page_contains("TEST2")
    assert_page_does_not_contain("TEST1")
    
    #Tests that cancel button works
    click_link_with_text("Set name")
    type_in_lone_text_field("TEST3")
    click_link_with_text("Cancel")
    wait_for_condition_text("TEST2")    
    assert_page_contains("TEST2")
    assert_page_does_not_contain("TEST1")
    assert_page_does_not_contain("TEST3")
    
   end
  
  def login
  #  open "/public/welcome"
  #  wait_for_page_to_load "30000"
  #  assert_page_contains("Patient Study Calendar - Public Test Site")
  #  click_link_with_text("Public Test Site - start page")
    open "/public/login"
    wait_for_page_to_load "30000"
    type("username", "superuser")
    type("password", "superuser")
    assert_page_contains("Please log in")
    click_button_with_text("Log in", 'submit')
    wait_for_page_to_load "30000"  
    assert_page_contains("Calendar Menu")
    assert_page_contains("Calendar templates")
  end   
end