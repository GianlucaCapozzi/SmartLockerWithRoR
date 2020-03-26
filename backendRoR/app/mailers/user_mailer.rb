class UserMailer < ActionMailer::Base

    public

    def registration_confirmation(user)
        @user = user
        @url = "https://smart-locker-macc.herokuapp.com/activation/"+@user.confirm_token
        mail(:from => "confirmation@smartlocker.com", :to => @user.email, :subject => "Registration Confirmation", :template_name => "registration_confirmation")
    end

    def recovery_password(user, temp_pass)
        @user = user
        @temp_pass = temp_pass
        mail(:from => "recovery@smartlocker.com", :to => @user.email, :subject => "Recovery Password", :template_name => "recovery_password")
    end

    def password_changed(user)
        @user = user
        mail(:from => "recovery@smartlocker.com", :to => @user.email, :subject => "Password Changed", :template_name => "password_changed")
    end

    def email_changed(old_mail, user)
        @user = user
        @old_mail = old_mail
        mail(:from => "recovery@smartlocker.com", :to => old_mail, :subject => "Email Changed", :template_name => "email_changed_old")
        mail(:from => "recovery@smartlocker.com", :to => @user.email, :subject => "Email Changed", :template_name => "email_changed_new")
    end
end