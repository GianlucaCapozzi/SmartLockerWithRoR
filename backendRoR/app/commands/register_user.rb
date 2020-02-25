class RegisterUser
    prepend SimpleCommand

    def initialize(email, password)
        @email = email
        @password = password
    end

    def call
        User.create(email: @email , password: @password , password_confirmation: @password) if signup_check
    end

    private

    attr_accessor :username, :email, :password

    def signup_check
        exist_email = User.exists?(email: @email)

        if exist_email
            errors.add :user_registration, 'email'
            return false
        else
            return true
        end
        
    end
end