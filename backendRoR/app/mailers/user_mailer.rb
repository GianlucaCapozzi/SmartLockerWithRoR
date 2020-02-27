class UserMailer < ActionMailer::Base
    default :from => "confirmation@smartlocker.com"

    public

    def registration_confirmation(user)
        @user = user
        @url = "http://localhost:3000/activation/"+@user.confirm_token
        mail(:to => @user.email, :subject => "Registration Confirmation")
    end

end